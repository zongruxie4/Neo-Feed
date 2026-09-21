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
import com.saulhdev.feeder.manager.miniflux.MinifluxClient
import com.saulhdev.feeder.manager.miniflux.MinifluxEntry
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.extractArticleImageFromHtml
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

private const val TAG = "MinifluxSource"

class MinifluxSource(
    private val account: AccountConfig.MinifluxAccount,
    private val client: MinifluxClient,
    private val syncQueueDao: SyncQueueDao,
    private val sourcesRepo: SourcesRepository,
    private val articleRepo: ArticleRepository,
) : NewsSource {

    private val syncMutex = Mutex()

    override val accountId: String = account.id
    override val accountType: AccountType = AccountType.MINIFLUX
    override val displayName: String = account.displayName.ifBlank {
        "Miniflux (${account.serverUrl.substringAfter("://").substringBefore("/")})"
    }

    override suspend fun syncPendingActions(): Boolean = withContext(Dispatchers.IO) {
        val actions = syncQueueDao.getPendingActions(account.id)
        if (actions.isEmpty()) return@withContext true

        Log.d(TAG, "Flushing ${actions.size} pending sync actions for account ${account.id}")
        val handledIds = mutableListOf<Long>()

        val markReadEntries = mutableListOf<Long>()
        val markReadActionIds = mutableListOf<Long>()

        val markUnreadEntries = mutableListOf<Long>()
        val markUnreadActionIds = mutableListOf<Long>()

        for (action in actions) {
            when (action.actionType) {
                SyncActionType.MARK_READ -> {
                    markReadEntries.add(action.remoteItemId)
                    markReadActionIds.add(action.id)
                }

                SyncActionType.MARK_UNREAD -> {
                    markUnreadEntries.add(action.remoteItemId)
                    markUnreadActionIds.add(action.id)
                }

                SyncActionType.STAR, SyncActionType.UNSTAR -> {
                    val res = client.toggleBookmark(
                        account.serverUrl,
                        account.apiToken,
                        action.remoteItemId
                    )
                    if (res.isSuccess) {
                        handledIds.add(action.id)
                    } else {
                        Log.w(
                            TAG,
                            "Failed to toggle bookmark for entry ${action.remoteItemId}: ${res.exceptionOrNull()?.message}"
                        )
                    }
                }
            }
        }

        if (markReadEntries.isNotEmpty()) {
            val res =
                client.markEntries(account.serverUrl, account.apiToken, markReadEntries, "read")
            if (res.isSuccess) {
                handledIds.addAll(markReadActionIds)
            } else {
                Log.w(TAG, "Failed batch mark read: ${res.exceptionOrNull()?.message}")
            }
        }

        if (markUnreadEntries.isNotEmpty()) {
            val res =
                client.markEntries(account.serverUrl, account.apiToken, markUnreadEntries, "unread")
            if (res.isSuccess) {
                handledIds.addAll(markUnreadActionIds)
            } else {
                Log.w(TAG, "Failed batch mark unread: ${res.exceptionOrNull()?.message}")
            }
        }

        if (handledIds.isNotEmpty()) {
            syncQueueDao.deleteActions(handledIds)
        }
        handledIds.size == actions.size
    }

    override suspend fun sync(context: Context, forceNetwork: Boolean): SyncResult =
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    Log.d(
                        TAG,
                        "Starting sync for Miniflux account ${account.id} at ${account.serverUrl}"
                    )

                    // Step 1: Flush pending queue first (bidirectional sync)
                    syncPendingActions()

                    // Step 2: Fetch remote feeds
                    val remoteFeedsResult = client.fetchFeeds(account.serverUrl, account.apiToken)
                    if (remoteFeedsResult.isFailure) {
                        val err = remoteFeedsResult.exceptionOrNull()?.message
                            ?: "Error desconocido obteniendo feeds"
                        Log.e(TAG, "Failed to fetch feeds: $err")
                        return@withContext SyncResult(success = false, errorMessage = err)
                    }

                    val remoteFeeds = remoteFeedsResult.getOrThrow()
                    Log.d(TAG, "Fetched ${remoteFeeds.size} remote feeds from Miniflux")

                    val localSources = sourcesRepo.getAllSources().toMutableList()
                    val remoteToLocalFeedId = mutableMapOf<Long, Long>()

                    for (rf in remoteFeeds) {
                        val categoryName = rf.category?.title ?: ""
                        val rawTitle = rf.title?.ifBlank { null } ?: rf.feedUrl?.ifBlank { null }
                        ?: "Miniflux Feed ${rf.id}"
                        val rawFeedUrl = rf.feedUrl?.ifBlank { null }
                            ?: "${account.serverUrl}#miniflux_feed_${rf.id}"
                        val strictUrl = sloppyLinkToStrictURL(rawFeedUrl)
                        val siteUrl = rf.siteUrl?.ifBlank { null } ?: ""

                        val existing = localSources.firstOrNull {
                            it.url.toString() == strictUrl.toString() ||
                                    ((it.sourceType == "miniflux" || it.sourceType == "rss") && it.title.equals(
                                        rawTitle,
                                        ignoreCase = true
                                    ))
                        }

                        val isFeedActive = account.isEnabled && !rf.disabled
                        val localFeedId = if (existing != null) {
                            val updated = existing.copy(
                                title = rawTitle,
                                tag = categoryName,
                                sourceType = "miniflux",
                                isEnabled = isFeedActive,
                                feedImage = if (siteUrl.isNotBlank()) sloppyLinkToStrictURLNoThrows(
                                    siteUrl
                                ) else existing.feedImage
                            )
                            sourcesRepo.updateSource(updated)
                            existing.id
                        } else {
                            val newFeed = Feed(
                                title = rawTitle,
                                description = "Miniflux feed",
                                url = strictUrl,
                                feedImage = if (siteUrl.isNotBlank()) sloppyLinkToStrictURLNoThrows(
                                    siteUrl
                                ) else sloppyLinkToStrictURLNoThrows(""),
                                sourceType = "miniflux",
                                tag = categoryName,
                                isEnabled = isFeedActive,
                            )
                            val insertedId = sourcesRepo.insertSource(newFeed)
                            localSources.add(newFeed.copy(id = insertedId))
                            insertedId
                        }
                        remoteToLocalFeedId[rf.id] = localFeedId
                    }

                    // Step 3: Fetch entries
                    val entriesResult = client.fetchEntries(
                        serverUrl = account.serverUrl,
                        apiToken = account.apiToken,
                        limit = 500,
                    )
                    if (entriesResult.isFailure) {
                        val err = entriesResult.exceptionOrNull()?.message
                            ?: "Error desconocido obteniendo artículos"
                        Log.e(TAG, "Failed to fetch entries: $err")
                        return@withContext SyncResult(success = false, errorMessage = err)
                    }

                    val remoteItems = entriesResult.getOrThrow().entries
                    Log.d(TAG, "Fetched ${remoteItems.size} items from Miniflux")

                    val downloadTime = Clock.System.now()
                    val converter = HtmlToPlainTextConverter()
                    val articlesToInsert = mutableListOf<Pair<Article, String>>()

                    for (item in remoteItems) {
                        val remoteFeedId =
                            if (item.feedId > 0) item.feedId else (item.feed?.id ?: 0L)
                        var localFeedId = remoteToLocalFeedId[remoteFeedId]

                        if (localFeedId == null && item.feed != null) {
                            val rf = item.feed
                            val categoryName = rf.category?.title ?: ""
                            val rawTitle =
                                rf.title?.ifBlank { null } ?: rf.feedUrl?.ifBlank { null }
                                ?: "Miniflux Feed ${rf.id}"
                            val rawFeedUrl = rf.feedUrl?.ifBlank { null }
                                ?: "${account.serverUrl}#miniflux_feed_${rf.id}"
                            val strictUrl = sloppyLinkToStrictURL(rawFeedUrl)
                            val siteUrl = rf.siteUrl?.ifBlank { null } ?: ""

                            val existing = localSources.firstOrNull {
                                it.url.toString() == strictUrl.toString() ||
                                        ((it.sourceType == "miniflux" || it.sourceType == "rss") && it.title.equals(
                                            rawTitle,
                                            ignoreCase = true
                                        ))
                            }
                            val isFeedActive = account.isEnabled && !rf.disabled
                            localFeedId = if (existing != null) {
                                val updated = existing.copy(
                                    title = rawTitle,
                                    tag = categoryName,
                                    sourceType = "miniflux",
                                    isEnabled = isFeedActive,
                                    feedImage = if (siteUrl.isNotBlank()) sloppyLinkToStrictURLNoThrows(
                                        siteUrl
                                    ) else existing.feedImage
                                )
                                sourcesRepo.updateSource(updated)
                                existing.id
                            } else {
                                val newFeed = Feed(
                                    title = rawTitle,
                                    description = "Miniflux feed",
                                    url = strictUrl,
                                    feedImage = if (siteUrl.isNotBlank()) sloppyLinkToStrictURLNoThrows(
                                        siteUrl
                                    ) else sloppyLinkToStrictURLNoThrows(""),
                                    sourceType = "miniflux",
                                    tag = categoryName,
                                    isEnabled = isFeedActive,
                                )
                                val insertedId = sourcesRepo.insertSource(newFeed)
                                localSources.add(newFeed.copy(id = insertedId))
                                insertedId
                            }
                            remoteToLocalFeedId[rf.id] = localFeedId
                        }

                        if (localFeedId == null) {
                            Log.w(
                                TAG,
                                "Dropping entry ${item.id} (${item.title}): Feed ID $remoteFeedId not found in local feeds"
                            )
                            continue
                        }

                        val itemGuid = item.id.toString()
                        val existingArticle = articleRepo.getArticleByGuid(itemGuid, localFeedId)

                        val article = normalizeMinifluxItem(
                            item = item,
                            feedId = localFeedId,
                            existing = existingArticle,
                            downloadTime = downloadTime,
                            converter = converter
                        )
                        articlesToInsert.add(article to (item.content ?: ""))
                    }

                    articleRepo.updateOrInsertArticle(articlesToInsert) { article, text ->
                        withContext(Dispatchers.IO) {
                            blobOutputStream(article.uuid, context.filesDir).bufferedWriter().use {
                                it.write(text)
                            }
                        }
                    }

                    Log.d(
                        TAG,
                        "Successfully synced ${articlesToInsert.size} articles from Miniflux"
                    )
                    SyncResult(success = true, itemsCount = articlesToInsert.size)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing Miniflux: ${e.message}", e)
                    SyncResult(success = false, errorMessage = e.message)
                }
            }
        }

    private fun normalizeMinifluxItem(
        item: MinifluxEntry,
        feedId: Long,
        existing: Article?,
        downloadTime: Instant,
        converter: HtmlToPlainTextConverter
    ): Article {
        val text = item.content ?: ""
        val plainSnippet = converter.convert(text).take(200)
        val dateStr = item.publishedAt?.ifBlank { null } ?: item.createdAt
        val pubDateMillis = if (!dateStr.isNullOrBlank()) {
            try {
                Instant.parse(dateStr).toEpochMilliseconds()
            } catch (e: Exception) {
                downloadTime.toEpochMilliseconds()
            }
        } else {
            downloadTime.toEpochMilliseconds()
        }
        val validPubDate =
            if (pubDateMillis > 0) pubDateMillis else downloadTime.toEpochMilliseconds()
        val plainTitle = (item.title?.ifBlank { null } ?: "Sin título").take(200)

        val uuid = existing?.uuid ?: ""

        val enclosureImage = item.enclosures?.firstOrNull { enc ->
            val mime = enc.mimeType?.lowercase() ?: ""
            val u = enc.url?.lowercase() ?: ""
            mime.startsWith("image/") || u.endsWith(".jpg") || u.endsWith(".jpeg") || u.endsWith(".png") || u.endsWith(
                ".webp"
            ) || u.endsWith(".gif")
        }?.url

        val htmlImage = extractArticleImageFromHtml(text, item.url)
        val finalImageUrl = enclosureImage ?: htmlImage ?: existing?.imageUrl
        val enclosureLink = item.enclosures?.firstOrNull()?.url ?: existing?.enclosureLink

        return Article(
            uuid = uuid,
            guid = item.id.toString(),
            title = plainTitle,
            plainTitle = plainTitle,
            plainSnippet = plainSnippet,
            description = text,
            imageUrl = finalImageUrl,
            enclosureLink = enclosureLink,
            author = item.author,
            pubDate = validPubDate,
            link = item.url,
            feedId = feedId,
            firstSyncedTime = existing?.firstSyncedTime ?: downloadTime,
            primarySortTime = Instant.fromEpochMilliseconds(validPubDate),
            bookmarked = item.starred,
            pinned = item.starred,
            isRead = item.status == "read",
        )
    }
}
