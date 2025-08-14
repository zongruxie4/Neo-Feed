/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
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

package com.saulhdev.feeder.manager.sync

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.db.ID_ALL
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedArticle
import com.saulhdev.feeder.data.entity.JsonFeed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.manager.models.FeedParser
import com.saulhdev.feeder.manager.models.getResponse
import com.saulhdev.feeder.manager.models.scheduleFullTextParse
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.java.KoinJavaComponent.inject
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.system.measureTimeMillis

val syncMutex = Mutex()
val prefs: FeedPreferences by inject(FeedPreferences::class.java)
val singleThreadedSync = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
const val TAG = "RssLocalSync"

suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    return syncMutex.withLock {
        withContext(singleThreadedSync) {
            syncFeeds(
                context = context,
                feedId = feedId,
                feedTag = feedTag,
                maxFeedItemCount = prefs.itemsPerFeed.getValue().toInt(),
                forceNetwork = forceNetwork,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        }
    }
}

internal suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    maxFeedItemCount: Int = 100,
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    var result = false
    val feedsRepo: SourcesRepository by inject(SourcesRepository::class.java)
    val articlesRepo: ArticleRepository by inject(ArticleRepository::class.java)
    val downloadTime = Instant.now()
    var needFullTextSync = false
    val time = measureTimeMillis {
        try {
            supervisorScope {
                val sRepository: SourcesRepository by inject(SourcesRepository::class.java)
                val staleTime: Long = if (forceNetwork) {
                    Instant.now().toEpochMilli()
                } else {
                    Instant.now().minus(minFeedAgeMinutes.toLong(), ChronoUnit.MINUTES)
                        .toEpochMilli()
                }

                val coroutineContext =
                    Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
                        Log.e(TAG, "Error during sync", throwable)
                    }

                val feedsToFetch = feedsToSync(
                    repository = sRepository,
                    feedId = feedId,
                    tag = feedTag,
                    staleTime = staleTime
                )
                val jobs = feedsToFetch.map {
                    needFullTextSync = needFullTextSync || it.fullTextByDefault
                    launch(coroutineContext) {
                        try {
                            feedsRepo.setCurrentlySyncingOn(
                                feedId = it.id,
                                syncing = true,
                                lastSync = Instant.now(),
                            )
                            syncFeed(
                                feedsRepo = feedsRepo,
                                articleRepo = articlesRepo,
                                feedSql = it,
                                filesDir = context.filesDir,
                                maxFeedItemCount = maxFeedItemCount,
                                forceNetwork = forceNetwork,
                                downloadTime = downloadTime
                            )
                        } catch (e: Throwable) {
                            Log.e(TAG, "Failed to sync ${it.title}: ${it.url}", e)
                        } finally {
                            Log.d(TAG, "Sync finished ${it.title}")
                            feedsRepo.setCurrentlySyncingOn(feedId = it.id, syncing = false)
                        }
                    }
                }

                jobs.joinAll()
                result = true

            }
        } catch (e: Throwable) {
            Log.e(TAG, "Outer error", e)
        } finally {
            if (needFullTextSync) {
                scheduleFullTextParse()
            }
        }
    }
    Log.d(TAG, "Completed in $time ms")
    return result
}

private suspend fun syncFeed(
    feedsRepo: SourcesRepository,
    articleRepo: ArticleRepository,
    feedSql: Feed,
    filesDir: File,
    maxFeedItemCount: Int,
    forceNetwork: Boolean = false,
    downloadTime: Instant
) {
    Log.d(TAG, "Fetching ${feedSql.title}")

    val okHttpClient = OkHttpClient.Builder()
        .build()
    val response: Response =
        okHttpClient.getResponse(url = feedSql.url, forceNetwork = forceNetwork)
    val feedParser = FeedParser()
    val feed: JsonFeed = response.use {
        response.body.let { responseBody ->
            when {
                !response.isSuccessful -> {
                    throw ResponseFailure("${response.code} when fetching ${feedSql.title}: ${feedSql.url}")
                }

                else                   -> {
                    Log.d(TAG, "Fetching correct ${feedSql.title}")
                    feedParser.parseFeedResponse(
                        url = response.request.url.toUrl(),
                        responseBody = responseBody
                    )
                }
            }
        }
    }.let {
        when {
            it.icon?.startsWith("data") == true -> it.copy(icon = null)
            else                                -> it
        }
    }

    val syncedFeed = feedSql.copy(lastSync = Instant.now())
    val items = feed.items

    val articles =
        items?.reversed()
            ?.map { item ->
                val article = (articleRepo.getArticleByGuid(
                    guid = item.id.toString(),
                    feedId = syncedFeed.id
                ) ?: FeedArticle(firstSyncedTime = downloadTime))
                    .updateFromParsedEntry(item, item.id.toString(), feed, syncedFeed.id)
                article to (item.content_html ?: item.content_text ?: "")
            } ?: emptyList()

    articleRepo.updateOrInsertArticle(articles) { feedItem, text ->
        withContext(Dispatchers.IO) {
            blobOutputStream(feedItem.id, filesDir).bufferedWriter().use {
                it.write(text)
            }
        }
    }

    feedsRepo.updateSource(
        syncedFeed.copy(
            title = syncedFeed.title,
            feedImage = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) }
                ?: syncedFeed.feedImage
        ))

    val ids = articleRepo.getItemsToBeCleanedFromFeed(
        feedId = syncedFeed.id,
        keepCount = max(maxFeedItemCount, items?.size ?: 0)
    )

    for (id in ids) {
        val file = blobFile(itemId = id, filesDir = filesDir)
        try {
            if (file.isFile) {
                file.delete()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to delete $file", e)
        }
    }

    articleRepo.deleteArticles(ids)
}

class ResponseFailure(message: String?) : Exception(message)

internal suspend fun feedsToSync(
    repository: SourcesRepository,
    feedId: Long,
    tag: String,
    staleTime: Long = -1L
): List<Feed> {

    return when {
        feedId > 0 -> {
            val feed = if (staleTime > 0) repository.loadFeedIfStale(
                feedId = feedId,
                staleTime = staleTime
            ) else repository.loadFeedById(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }

        feedId == ID_ALL -> repository.loadFeedIds().mapNotNull {
            if (staleTime > 0) repository.loadFeedIfStale(
                feedId = feedId,
                staleTime = staleTime
            ) else repository.loadFeedById(feedId)
        }

        else -> repository.getAllSources()
    }
}
