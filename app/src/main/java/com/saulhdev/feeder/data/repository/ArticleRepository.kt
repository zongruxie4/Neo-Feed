/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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

package com.saulhdev.feeder.data.repository

import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.db.dao.insertOrUpdate
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedArticle
import com.saulhdev.feeder.data.entity.FeedItemIdWithLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleRepository(db: NeoFeedDb) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()
    private val articlesDao = db.feedArticleDao()
    private val feedsDao = db.feedSourceDao()

    suspend fun deleteArticles(ids: List<Long>) = withContext(jcc) {
        articlesDao.deleteArticles(ids)
    }

    suspend fun getArticleByGuid(guid: String, feedId: Long): FeedArticle? {
        return withContext(jcc) {
            articlesDao.loadArticle(guid = guid, feedId = feedId)
        }
    }

    fun getArticleById(articleId: Long): Flow<FeedArticle?> =
        articlesDao.loadArticleById(id = articleId)
            .flowOn(cc)

    fun getEnabledFeedArticles(): Flow<List<FeedArticle>> = articlesDao.getAllEnabledFeedArticles()
        .flowOn(cc)

    fun getFeedByTags(tags: Set<String>): Flow<List<Feed>> = feedsDao.getFeedByTags(tags)
        .flowOn(cc)

    suspend fun updateOrInsertArticle(
        itemsWithText: List<Pair<FeedArticle, String>>,
        block: suspend (FeedArticle, String) -> Unit
    ) = withContext(jcc) {
        articlesDao.insertOrUpdate(itemsWithText, block)
    }

    suspend fun bookmarkArticle(
        articleId: Long,
        bookmark: Boolean,
    ) = withContext(jcc) {
        articlesDao.getArticleById(articleId)?.let {
            articlesDao.updateFeedArticle(it.copy(bookmarked = bookmark, pinned = bookmark))
        }
    }

    suspend fun unpinArticle(
        articleId: Long,
        pin: Boolean = false,
    ) = withContext(jcc) {
        articlesDao.getArticleById(articleId)?.let {
            articlesDao.updateFeedArticle(it.copy(pinned = pin))
        }
    }

    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int) = withContext(jcc) {
        articlesDao.getItemsToBeCleanedFromFeed(feedId = feedId, keepCount = keepCount)
    }

    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>> =
        articlesDao.getFeedsItemsWithDefaultFullTextParse()
            .flowOn(cc)

    fun getBookmarkedArticlesMap(): Flow<Map<FeedArticle, Feed>> = articlesDao.getAllBookmarked()
        .mapLatest {
            it.associateWith { fa ->
                feedsDao.findFeedById(fa.feedId).first()
            }
        }
        .flowOn(cc)

    fun getBookmarkedArticles(): Flow<List<FeedArticle>> = articlesDao.getAllBookmarked()
        .flowOn(cc)
}