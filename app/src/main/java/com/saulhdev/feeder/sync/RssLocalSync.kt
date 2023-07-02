package com.saulhdev.feeder.sync

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedArticle
import com.saulhdev.feeder.db.FeedDao
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.db.ID_UNSET
import com.saulhdev.feeder.models.FeedParser
import com.saulhdev.feeder.models.getResponse
import com.saulhdev.feeder.models.scheduleFullTextParse
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobOutputStream
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import com.saulhdev.jsonfeed.JsonFeed
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
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.system.measureTimeMillis

val syncMutex = Mutex()
val singleThreadedSync = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
const val TAG = "RssLocalSync"

suspend fun syncFeeds(
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    val prefs = FeedPreferences(context)
    val di: DI by closestDI(context)
    return syncMutex.withLock {
        withContext(singleThreadedSync) {
            syncFeeds(
                di,
                context = context,
                feedId = feedId,
                feedTag = feedTag,
                maxFeedItemCount = prefs.itemsPerFeed.onGetValue().toInt(),
                forceNetwork = forceNetwork,
                minFeedAgeMinutes = minFeedAgeMinutes
            )
        }
    }
}

internal suspend fun syncFeeds(
    di: DI,
    context: Context,
    feedId: Long = ID_UNSET,
    feedTag: String = "",
    maxFeedItemCount: Int = 50,
    forceNetwork: Boolean = false,
    minFeedAgeMinutes: Int = 5
): Boolean {
    var result = false
    val repository = FeedRepository(context)
    val downloadTime = Instant.now()
    var needFullTextSync = false
    val time = measureTimeMillis {
        try {
            supervisorScope {
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

                val feedsToFetch =
                    feedsToSync(repository.feedDao, feedId, feedTag, staleTime = staleTime)
                val jobs = feedsToFetch.map {
                    needFullTextSync = needFullTextSync || it.fullTextByDefault
                    launch(coroutineContext) {
                        try {
                            repository.setCurrentlySyncingOn(
                                feedId = it.id,
                                syncing = true,
                                lastSync = Instant.now(),
                            )
                            syncFeed(
                                repository = repository,
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
                            repository.setCurrentlySyncingOn(feedId = it.id, syncing = false)
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
                scheduleFullTextParse(di = di)
            }
        }
    }
    Log.d(TAG, "Completed in $time ms")
    return result
}

private suspend fun syncFeed(
    repository: FeedRepository,
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

                else -> {
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
            else -> it
        }
    }

    feedSql.lastSync = Instant.now()
    val items = feed.items

    val articles =
        items?.reversed()
            ?.map { item ->
                val article = repository.getArticleByGuid(
                    guid = item.id.toString(),
                    feedId = feedSql.id
                ) ?: FeedArticle(firstSyncedTime = downloadTime)

                article.updateFromParsedEntry(item, item.id.toString(), feed)
                article.feedId = feedSql.id
                article to (item.content_html ?: item.content_text ?: "")
            } ?: emptyList()
    Log.d(TAG, "Articles  ${articles.size}")

    repository.updateOrInsertArticle(articles) { feedItem, text ->
        withContext(Dispatchers.IO) {
            blobOutputStream(feedItem.id, filesDir).bufferedWriter().use {
                it.write(text)
            }
        }
    }
    feedSql.title = feedSql.title

    feedSql.feedImage = feed.icon?.let { sloppyLinkToStrictURLNoThrows(it) }
        ?: feedSql.feedImage
    repository.updateFeed(feedSql)

    val ids = repository.getItemsToBeCleanedFromFeed(
        feedId = feedSql.id,
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

    repository.deleteArticles(ids)
}

class ResponseFailure(message: String?) : Exception(message)

internal suspend fun feedsToSync(
    feedDao: FeedDao,
    feedId: Long,
    tag: String,
    staleTime: Long = -1L
): List<Feed> {
    return when {
        feedId > 0 -> {
            val feed = if (staleTime > 0) feedDao.loadFeedIfStale(
                feedId,
                staleTime = staleTime
            ) else feedDao.loadFeed(feedId)
            if (feed != null) {
                listOf(feed)
            } else {
                emptyList()
            }
        }
        /*tag.isNotEmpty() -> if (staleTime > 0) feedDao.loadFeedsIfStale(
            tag = tag,
            staleTime = staleTime
        ) else feedDao.loadFeeds(tag)
        else -> if (staleTime > 0) feedDao.loadFeedsIfStale(staleTime) else feedDao.loadFeeds()

        feedId > 0 -> {
            feedDao.loadFeed(feedId)?.let { listOf(it) } ?: emptyList()
        }*/

        else -> feedDao.loadFeeds()
    }
}
