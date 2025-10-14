package com.saulhdev.feeder.manager.models

import com.saulhdev.feeder.data.db.dao.FeedSourceDao
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OPMLToRoom() : OPMLParserToDatabase, KoinComponent {
    private val dao: FeedSourceDao by inject()

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