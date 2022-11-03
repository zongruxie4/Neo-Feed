/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.db

import android.content.Context
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import java.net.URL

class FeedRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedRepository")
    private val feedDao = NeoFeedDb.getInstance(context).feedDao()
    private val feedArticleDao = NeoFeedDb.getInstance(context).feedArticleDao()

    fun insertFeed(feed: Feed) {
        scope.launch {
            feedDao.insert(feed)
        }
    }

    fun updateFeed(title: String, url: URL) {
        scope.launch {
            val list: List<Feed> = feedDao.findFeed(title, url)
            if (list.isEmpty()) {
                val feed = list.first()
                feed.title = title
                feed.url = url
                feedDao.update(feed)
            }
        }
    }

    fun updateFeed(feed: Feed) {
        scope.launch {
            val list: List<Feed> = feedDao.findFeed(feed.id)
            if (list.isNotEmpty()) {
                feed.lastSync = ZonedDateTime.now().toInstant()
                feedDao.update(feed)
            }
        }
    }

    fun getAllFeeds(): List<Feed> {
        return feedDao.loadFeeds()
    }

    fun deleteFeed(feed: Feed) {
        scope.launch {
            feedArticleDao.deleteFeedArticle(feed.id)
            feedDao.delete(feed)
        }
    }

    suspend fun getFeedArticles(feed: Feed): ArrayList<FeedArticle> = withContext(Dispatchers.IO) {
        val list: ArrayList<FeedArticle> = arrayListOf()
        list.addAll(feedArticleDao.loadArticles(feed.id))
        list
    }

    fun updateOrInsertArticle(articles: List<FeedArticle>) {
        scope.launch {
            articles.forEach { article ->
                val dbArticleDao: FeedArticle? = feedArticleDao.findArticle(article.guid)
                if (dbArticleDao == null) {
                    feedArticleDao.insertFeedArticle(article)
                } else {
                    dbArticleDao.title = article.title
                    dbArticleDao.description = article.description
                    dbArticleDao.content_html = article.content_html
                    dbArticleDao.imageUrl = article.imageUrl
                    dbArticleDao.link = article.link
                    dbArticleDao.feedId = article.feedId
                    dbArticleDao.pubDate = article.pubDate
                    dbArticleDao.categories = article.categories
                    feedArticleDao.updateFeedArticle(dbArticleDao)
                }
            }
        }
    }
}