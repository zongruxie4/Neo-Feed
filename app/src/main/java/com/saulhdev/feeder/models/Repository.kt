package com.saulhdev.feeder.models

import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@OptIn(ExperimentalCoroutinesApi::class)
class Repository(override val di: DI) : DIAware {
    private val feedStore: FeedStore by instance()

    suspend fun getFeed(feedId: Long): Feed? = feedStore.getFeed(feedId)
}