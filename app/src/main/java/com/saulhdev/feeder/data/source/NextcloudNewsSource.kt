/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.data.source

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.data.account.AccountType
import com.saulhdev.feeder.data.db.dao.SyncQueueDao
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.SyncActionType
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.nextcloud.NextcloudItem
import com.saulhdev.feeder.manager.nextcloud.NextcloudNewsClient
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.extractArticleImageFromHtml
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

private const val TAG = "NextcloudNewsSource"

class NextcloudNewsSource(
    private val account: AccountConfig.NextcloudNewsAccount,
    private val client: NextcloudNewsClient,
    private val syncQueueDao: SyncQueueDao,
    private val sourcesRepo: SourcesRepository,
    private val articleRepo: ArticleRepository,
) : NewsSource {

    override val accountId: String = account.id
    override val accountType: AccountType = AccountType.NEXTCLOUD_NEWS
    override val displayName: String = account.displayName.ifBlank {
        "${account.username}@${account.serverUrl.substringAfter("://").substringBefore("/")}"
    }

    override suspend fun syncPendingActions(): Boolean = withContext(Dispatchers.IO) {
        val actions = syncQueueDao.getPendingActions(account.id)
        if (actions.isEmpty()) return@withContext true

        Log.d(TAG, "Flushing ${actions.size} pending sync actions for account ${account.id}")
        val handledIds = mutableListOf<Long>()

        for (action in actions) {
            val res = when (action.actionType) {
                SyncActionType.MARK_READ -> client.markItemRead(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken,
                    action.remoteItemId
                )

                SyncActionType.MARK_UNREAD -> client.markItemUnread(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken,
                    action.remoteItemId
                )

                SyncActionType.STAR -> client.starItem(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken,
                    action.remoteItemId
                )

                SyncActionType.UNSTAR -> client.unstarItem(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken,
                    action.remoteItemId
                )

                else -> Result.success(Unit)
            }

            if (res.isSuccess) {
                handledIds.add(action.id)
            } else {
                Log.w(
                    TAG,
                    "Failed action ${action.actionType} for item ${action.remoteItemId}: ${res.exceptionOrNull()?.message}"
                )
            }
        }

        if (handledIds.isNotEmpty()) {
            syncQueueDao.deleteActions(handledIds)
        }
        handledIds.size == actions.size
    }

    override suspend fun sync(context: Context, forceNetwork: Boolean): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                syncPendingActions()
                val folders = client.fetchFolders(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken
                )
                    .getOrDefault(emptyList())
                val folderMap = folders.associate { it.id to it.name }

                val remoteFeeds =
                    client.fetchFeeds(account.serverUrl, account.username, account.passwordOrToken)
                        .getOrThrow()

                val localSources = sourcesRepo.getAllSources()
                val remoteToLocalFeedId = mutableMapOf<Long, Long>()

                for (rf in remoteFeeds) {
                    val folderName = rf.folderId?.let { folderMap[it] } ?: ""
                    if (account.excludedFolders.contains(folderName)) {
                        continue
                    }

                    val feedUrl = rf.url.ifBlank { "${account.serverUrl}#nextcloud_feed_${rf.id}" }
                    val strictUrl = sloppyLinkToStrictURL(feedUrl)

                    val existing = localSources.firstOrNull {
                        it.url == strictUrl || (it.sourceType == "nextcloud_news" && it.title == rf.title)
                    }

                    val localFeedId = if (existing != null) {
                        val updated = existing.copy(
                            title = rf.title,
                            tag = folderName,
                            isEnabled = existing.isEnabled,
                            feedImage = rf.faviconLink?.let { sloppyLinkToStrictURLNoThrows(it) }
                                ?: existing.feedImage
                        )
                        sourcesRepo.updateSource(updated)
                        existing.id
                    } else {
                        val newFeed = Feed(
                            title = rf.title,
                            description = "Nextcloud News feed",
                            url = strictUrl,
                            feedImage = rf.faviconLink?.let { sloppyLinkToStrictURLNoThrows(it) }
                                ?: sloppyLinkToStrictURLNoThrows(""),
                            sourceType = "nextcloud_news",
                            tag = folderName,
                        )
                        sourcesRepo.insertSource(newFeed)
                    }
                    remoteToLocalFeedId[rf.id] = localFeedId
                }

                val remoteItems = client.fetchItems(
                    account.serverUrl,
                    account.username,
                    account.passwordOrToken,
                    batchSize = 200,
                    getRead = true
                ).getOrThrow()

                Log.d(TAG, "Fetched ${remoteItems.size} items from Nextcloud News")

                val downloadTime = Clock.System.now()
                val converter = HtmlToPlainTextConverter()

                val articlesToInsert = mutableListOf<Pair<Article, String>>()

                for (item in remoteItems) {
                    val localFeedId = remoteToLocalFeedId[item.feedId] ?: continue
                    val itemGuid = item.id.toString()
                    val existingArticle = articleRepo.getArticleByGuid(itemGuid, localFeedId)

                    val article = normalizeNextcloudItem(
                        item = item,
                        feedId = localFeedId,
                        existing = existingArticle,
                        downloadTime = downloadTime,
                        converter = converter
                    )
                    articlesToInsert.add(article to (item.body ?: ""))
                }

                articleRepo.updateOrInsertArticle(articlesToInsert) { article, text ->
                    withContext(Dispatchers.IO) {
                        blobOutputStream(article.uuid, context.filesDir).bufferedWriter().use {
                            it.write(text)
                        }
                    }
                }

                SyncResult(success = true, itemsCount = articlesToInsert.size)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing Nextcloud News: ${e.message}", e)
                SyncResult(success = false, errorMessage = e.message)
            }
        }

    private fun normalizeNextcloudItem(
        item: NextcloudItem,
        feedId: Long,
        existing: Article?,
        downloadTime: Instant,
        converter: HtmlToPlainTextConverter
    ): Article {
        val text = item.body ?: ""
        val plainSnippet = converter.convert(text).take(200)
        val pubDateMillis =
            if (item.pubDate > 0) item.pubDate * 1000L else downloadTime.toEpochMilliseconds()
        val plainTitle = item.title?.take(200) ?: ""

        val uuid = existing?.uuid ?: ""

        val isEnclosureImage = item.enclosureMime?.startsWith("image/") == true ||
                item.enclosureLink?.let { u ->
                    val lower = u.lowercase()
                    lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(
                        ".webp"
                    ) || lower.endsWith(".gif")
                } == true
        val enclosureImage = if (isEnclosureImage) item.enclosureLink else null
        val htmlImage = extractArticleImageFromHtml(text, item.url)
        val finalImageUrl = enclosureImage ?: htmlImage ?: existing?.imageUrl

        return Article(
            uuid = uuid,
            guid = item.id.toString(),
            title = plainTitle,
            plainTitle = plainTitle,
            plainSnippet = plainSnippet,
            description = text,
            imageUrl = finalImageUrl,
            enclosureLink = item.enclosureLink ?: existing?.enclosureLink,
            author = item.author,
            pubDate = pubDateMillis,
            link = item.url,
            feedId = feedId,
            firstSyncedTime = existing?.firstSyncedTime ?: downloadTime,
            primarySortTime = Instant.fromEpochMilliseconds(pubDateMillis),
            bookmarked = item.starred,
            pinned = item.starred,
            isRead = !item.unread,
        )
    }
}
