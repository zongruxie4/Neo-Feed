package com.saulhdev.feeder.manager.models

import com.saulhdev.feeder.data.db.models.Feed

interface OPMLParserToDatabase {
    suspend fun getFeed(url: String): Feed?

    suspend fun saveFeed(feed: Feed)
}
