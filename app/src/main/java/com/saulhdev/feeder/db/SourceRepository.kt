package com.saulhdev.feeder.db

import android.content.Context
import com.saulhdev.feeder.db.models.Feed
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SourceRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("FeedSourceRepository")
    private val sourceDao = NeoFeedDb.getInstance(context).feedSourceDao()

    fun getAllFeeds(): Flow<List<Feed>> {
        return sourceDao.getAllFeeds()
    }

    suspend fun loadFeeds(tag: String): List<Feed> {
        return sourceDao.loadFeeds(tag)
    }

    suspend fun loadTags(): List<String> {
        return sourceDao.loadTags()
    }

    suspend fun loadFeedIfStale(feedId: Long, staleTime: Long): Feed? {
        return sourceDao.loadFeedIfStale(feedId, staleTime)
    }

    suspend fun loadFeed(feedId: Long): Feed? {
        return sourceDao.loadFeedById(feedId)
    }

    fun loadFeeds(): List<Feed> {
        return sourceDao.loadFeeds()
    }

    fun deleteFeed(value: Feed) {
        scope.launch {
            sourceDao.delete(value)
        }
    }
}