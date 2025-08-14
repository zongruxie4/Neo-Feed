/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.saulhdev.feeder.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedArticle
import com.saulhdev.feeder.data.db.models.FeedItem
import com.saulhdev.feeder.data.entity.FeedItemIdWithLink
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedArticle(item: FeedArticle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeedArticle(items: List<FeedArticle>): List<Long>

    @Update
    suspend fun updateFeedArticle(item: FeedArticle): Int

    @Update
    suspend fun updateFeedArticle(items: List<FeedArticle>): Int

    @Delete
    suspend fun deleteFeedArticle(item: FeedArticle): Int

    @Query(
        """
        DELETE FROM FeedArticle WHERE id IN (:ids)
        """
    )
    suspend fun deleteArticles(ids: List<Long>): Int

    @Query(
        """
        DELETE FROM FeedArticle WHERE id IN (:ids)
        """
    )
    suspend fun deleteFeedArticle(ids: List<Long>): Int

    @Query(
        """
        DELETE FROM FeedArticle WHERE feedId = :feedId
        """
    )
    suspend fun deleteFeedArticle(feedId: Long?): Int

    @Query("SELECT * FROM feedArticle WHERE guid IS :guid AND feedId IS :feedId")
    suspend fun loadArticle(guid: String, feedId: Long?): FeedArticle?

    @Query("SELECT * FROM feedArticle WHERE guid IS :guid")
    suspend fun loadArticleByGuid(guid: String): FeedArticle?

    @Query("SELECT * FROM feedArticle WHERE id IS :id")
    suspend fun getArticleById(id: Long): FeedArticle?

    @Query("SELECT * FROM feedArticle WHERE id IS :id")
    fun loadArticleById(id: Long): Flow<FeedArticle?>

    @Query("SELECT * FROM feedArticle WHERE feedId IS :feedId")
    suspend fun loadArticles(feedId: Long?): List<FeedArticle>

    @Query(
        """
        SELECT FeedArticle.* FROM FeedArticle
        JOIN Feeds ON FeedArticle.feedId = Feeds.id
        WHERE Feeds.isEnabled = 1
    """
    )
    fun getAllEnabledFeedArticles(): Flow<List<FeedArticle>>

    @Query(
        """
        SELECT id FROM FeedArticle
        WHERE feedId IS :feedId AND pinned = 0 AND bookmarked = 0
        ORDER BY primarySortTime DESC, pubDate DESC
        LIMIT -1 OFFSET :keepCount
        """
    )
    suspend fun getItemsToBeCleanedFromFeed(feedId: Long, keepCount: Int): List<Long>

    @Query(
        """
            SELECT fi.id, fi.link
            FROM feedArticle fi
            JOIN feeds f ON fi.feedId = f.id
            WHERE f.fulltextByDefault = 1 OR fi.bookmarked = 1
        """
    )
    fun getFeedsItemsWithDefaultFullTextParse(): Flow<List<FeedItemIdWithLink>>

    @Query(
        """
            SELECT *
            FROM feedArticle fi
            WHERE fi.bookmarked = 1
            ORDER BY pinned DESC, pubDate DESC
        """
    )
    fun getAllBookmarked(): Flow<List<FeedArticle>>

    // Embedded FeedItem
    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE Feeds.isEnabled = 1
    ORDER BY FeedArticle.primarySortTime DESC
    """
    )
    fun getAllEnabledFeedItems(): Flow<List<FeedItem>>

    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE FeedArticle.bookmarked = 1 AND Feeds.isEnabled = 1
    ORDER BY FeedArticle.pinned DESC, FeedArticle.pubDate DESC
    """
    )
    fun getAllBookmarkedFeedItems(): Flow<List<FeedItem>>

    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE Feeds.isEnabled = 1 AND Feeds.tag IN (:tags)
    ORDER BY FeedArticle.primarySortTime DESC
    """
    )
    fun getFeedItemsByTagsSimple(tags: Set<String>): Flow<List<FeedItem>>

    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE FeedArticle.feedId = :feedId AND Feeds.isEnabled = 1
    ORDER BY FeedArticle.primarySortTime DESC
    """
    )
    fun getFeedItemsForFeed(feedId: Long): Flow<List<FeedItem>>

    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE FeedArticle.pinned = 1 AND Feeds.isEnabled = 1
    ORDER BY FeedArticle.primarySortTime DESC
    """
    )
    fun getPinnedFeedItems(): Flow<List<FeedItem>>

    @Query("SELECT * FROM Feeds WHERE ',' || tag || ',' LIKE :pattern AND isEnabled = 1")
    suspend fun getEnabledFeedsByTagPattern(pattern: String): List<Feed>

    @Transaction
    suspend fun getFeedItemsByTagsComplex(tags: Set<String>): List<FeedItem> {
        val feedIds = tags.flatMap { tag ->
            getEnabledFeedsByTagPattern("%,$tag,%")
        }.distinctBy { it.id }.map { it.id }

        return getFeedItemsByFeedIds(feedIds)
    }

    @Query(
        """
    SELECT FeedArticle.*, Feeds.* FROM FeedArticle
    JOIN Feeds ON FeedArticle.feedId = Feeds.id
    WHERE FeedArticle.feedId IN (:feedIds) AND Feeds.isEnabled = 1
    ORDER BY FeedArticle.primarySortTime DESC
    """
    )
    suspend fun getFeedItemsByFeedIds(feedIds: List<Long>): List<FeedItem>

    @Transaction
    suspend fun insertOrUpdate(
        itemsWithText: List<Pair<FeedArticle, String>>,
        block: suspend (FeedArticle, String) -> Unit
    ) {
        val updatedItems = itemsWithText.filter { (item, _) ->
            item.id > ID_UNSET
        }
        updateFeedArticle(updatedItems.map { (item, _) -> item })

        val insertedItems = itemsWithText.filter { (item, _) ->
            item.id <= ID_UNSET
        }
        val insertedIds = insertFeedArticle(insertedItems.map { (item, _) -> item })

        updatedItems.forEach { (item, text) ->
            block(item, text)
        }

        insertedIds.zip(insertedItems).forEach { (itemId, itemToText) ->
            val (item, text) = itemToText
            block(item.copy(id = itemId), text)
        }
    }
}
