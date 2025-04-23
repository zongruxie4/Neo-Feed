package com.saulhdev.feeder.data.repository

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.manager.models.scheduleFullTextParse
import com.saulhdev.feeder.manager.sync.FeedSyncer
import com.saulhdev.feeder.manager.sync.requestFeedSync
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import java.net.URL

class FeedRepository(db: NeoFeedDb) {
    private val jcc = Dispatchers.IO + SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("FeedSourceRepository")
    private val feedsDao = db.feedSourceDao()
    private val workManager: WorkManager by inject(WorkManager::class.java)

    suspend fun insertFeed(feed: Feed) = withContext(jcc) {
        feedsDao.insert(feed)
    }

    suspend fun updateFeed(
        title: String,
        url: URL,
        fullTextByDefault: Boolean,
        isEnabled: Boolean
    ) = withContext(jcc) {
        feedsDao.getFeedByURL(url)
            ?.copy(
                title = title,
                url = url,
                fullTextByDefault = fullTextByDefault,
                isEnabled = isEnabled
            )?.let {
                feedsDao.update(it)
            }
    }

    fun updateFeed(feed: Feed, resync: Boolean = false) {
        scope.launch {
            val list: List<Feed> = feedsDao.findFeedById(feed.id)
            if (list.isNotEmpty()) {
                feed.lastSync = ZonedDateTime.now().toInstant()
                feedsDao.update(feed)
                if (resync) requestFeedSync(feed.id)
                if (feed.fullTextByDefault) scheduleFullTextParse()
            }
        }
    }

    fun getAllFeeds(): Flow<List<Feed>> = feedsDao.getAllFeeds()

    fun getEnabledFeeds(): Flow<List<Feed>> = feedsDao.getEnabledFeeds()

    fun getFeedById(id: Long): Flow<Feed?> = feedsDao.getFeedById(id)

    // TODO make suspend
    fun loadFeeds(): List<Feed> = feedsDao.loadFeeds()

    suspend fun loadFeedsByTag(tag: String): List<Feed> = withContext(jcc) {
        feedsDao.loadFeedsByTag(tag)
    }

    suspend fun loadFeedById(feedId: Long): Feed? = withContext(jcc) {
        feedsDao.loadFeedById(feedId)
    }

    suspend fun loadFeedIfStale(feedId: Long, staleTime: Long): Feed? = withContext(jcc) {
        feedsDao.loadFeedIfStale(feedId, staleTime)
    }

    suspend fun loadTags(): List<String> {
        return feedsDao.loadTags()
    }

    fun loadFeedIds(): List<Long> {
        return feedsDao.loadFeedIds()
    }

    val isSyncing: StateFlow<Boolean> =
        workManager.getWorkInfosByTagFlow(FeedSyncer::class.qualifiedName!!)
            .map {
                workManager.pruneWork()
                it.any { work ->
                    work.state == WorkInfo.State.RUNNING || work.state == WorkInfo.State.BLOCKED
                }
            }
            .debounce(1000L)
            .stateIn(
                scope,
                SharingStarted.Lazily,
                false
            )

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean) {
        scope.launch {
            feedsDao.setCurrentlySyncingOn(feedId, syncing)
        }
    }

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: Instant) {
        scope.launch {
            feedsDao.setCurrentlySyncingOn(feedId, syncing, lastSync)
        }
    }

    fun deleteFeed(value: Feed) {
        scope.launch {
            feedsDao.delete(value)
        }
    }
}