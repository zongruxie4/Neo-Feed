package com.saulhdev.feeder.models

import com.saulhdev.feeder.db.models.Feed

interface OPMLParserToDatabase {
    suspend fun getFeed(url: String): Feed?

    suspend fun saveFeed(feed: Feed)
}
