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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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
        DELETE FROM feedArticle WHERE id IN (:ids)
        """
    )
    suspend fun deleteFeedArticle(ids: List<Long>): Int

    @Query("SELECT * FROM feedArticle WHERE guid IS :guid AND feedId IS :feedId")
    suspend fun loadFeedItem(guid: String, feedId: Long?): FeedArticle?

}