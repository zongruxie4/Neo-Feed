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
import android.util.Log
import com.saulhdev.feeder.db.dao.insertOrUpdate
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.db.models.FeedArticle
import com.saulhdev.feeder.db.models.FeedItemIdWithLink
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import java.net.URL

class ArticleRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("FeedArticleRepository")
    private val articleDao = NeoFeedDb.getInstance(context).feedSourceDao()

    fun insertFeed(feed: Feed) {
        scope.launch {
            articleDao.insert(feed)
        }
    }

    fun updateFeed(title: String, url: URL, fullTextByDefault: Boolean, isEnabled: Boolean) {
        scope.launch {
            val feed = articleDao.getFeedByURL(url)
            Log.d("FeedRepository", "updateFeed: ${feed?.url}")
            if (feed != null) {
                feed.title = title
                feed.url = url
                feed.fullTextByDefault = fullTextByDefault
                feed.isEnabled = isEnabled
                articleDao.update(feed)
            }
        }
    }

    fun updateFeed(feed: Feed) {
        scope.launch {
            val list: List<Feed> = articleDao.findFeedById(feed.id)
            if (list.isNotEmpty()) {
                feed.lastSync = ZonedDateTime.now().toInstant()
                articleDao.update(feed)
            }
        }
    }

    suspend fun getFeed(feedId: Long): List<Feed> = articleDao.findFeedById(feedId)

    fun getAllFeeds(): List<Feed> {
        return articleDao.loadFeeds()
    }

    fun getFeedById(id: Long): Flow<Feed> {
        return articleDao.getFeedById(id)
    }

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean) {
        scope.launch {
            articleDao.setCurrentlySyncingOn(feedId, syncing)
        }
    }

    fun setCurrentlySyncingOn(feedId: Long, syncing: Boolean, lastSync: Instant) {
        scope.launch {
            articleDao.setCurrentlySyncingOn(feedId, syncing, lastSync)
        }
    }

    /* Articles */
    private val feedArticleDao = NeoFeedDb.getInstance(context).feedArticleDao()

    suspend fun getFeedArticles(feed: Feed): ArrayList<FeedArticle> = withContext(Dispatchers.IO) {
        val list: ArrayList<FeedArticle> = arrayListOf()
        list.addAll(feedArticleDao.loadArticles(feed.id))
        list
    }

    suspend fun deleteArticles(ids: List<Long>) {
        feedArticleDao.deleteArticles(ids)
    }

    suspend fun getArticleByGuid(guid: String, feedId: Long): FeedArticle? {
        return feedArticleDao.loadArticle(guid = guid, feedId = feedId)
    }

    fun getArticleById(feedId: Long): Flow<FeedArticle?> {
        return feedArticleDao.loadArticleById(id = feedId)
    }

    suspend fun updateOrInsertArticle(
        itemsWithText: List<Pair<FeedArticle, String>>,
        block: suspend (FeedArticle, String) -> Unit
    ) {
        feedArticleDao.insertOrUpdate(itemsWithText, block)
    }

    suspend fun bookmarkArticle(
        articleId: Long,
        bookmark: Boolean,
    ) = feedArticleDao.getArticleById(articleId)?.let {
        feedArticleDao.updateFeedArticle(it.copy(bookmarked = bookmark, pinned = bookmark))
    }

    suspend fun unpinArticle(
        articleId: Long,
        pin: Boolean = false,
    ) = feedArticleDao.getArticleById(articleId)?.let {
        feedArticleDao.updateFeedArticle(it.copy(pinned = pin))
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int) =
        feedArticleDao.getItemsToBeCleanedFromFeed(feedId = feedId, keepCount = keepCount)

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>> =
        feedArticleDao.getFeedsItemsWithDefaultFullTextParse()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getBookmarkedArticlesMap(): Flow<Map<FeedArticle, Feed>> =
        feedArticleDao.allBookmarked.mapLatest {
            it.associateWith { fa ->
                articleDao.findFeedById(fa.feedId).first()
            }
        }
}