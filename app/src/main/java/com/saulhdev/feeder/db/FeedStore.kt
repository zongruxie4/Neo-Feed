package com.saulhdev.feeder.db

import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class FeedStore(override val di: DI) : DIAware {
    private val feedDao: FeedDao by instance()

    suspend fun getFeed(feedId: Long): Feed? = feedDao.loadFeed(feedId)
}