package com.saulhdev.feeder.models

import com.saulhdev.feeder.db.NeoFeedDb
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows

class OPMLToRoom(db: NeoFeedDb) : OPMLParserToDatabase {

    private val dao = db.feedSourceDao()

    override suspend fun getFeed(url: String): Feed? =
        dao.getFeedByURL(sloppyLinkToStrictURLNoThrows(url))

    override suspend fun saveFeed(feed: Feed) {
        val existing = dao.getFeedByURL(feed.url)

        // Don't want to remove existing feed on OPML imports
        if (existing != null) {
            dao.update(feed)
        } else {
            dao.insert(feed)
        }
    }
}