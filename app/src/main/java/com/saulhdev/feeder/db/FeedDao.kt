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
import java.net.URL

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: Feed): Long

    @Update
    suspend fun update(feed: Feed): Int

    @Delete
    suspend fun delete(feed: Feed): Int

    @Query("SELECT * FROM Feeds WHERE title = :title AND url = :url")
    suspend fun findFeed(title: String, url: URL): List<Feed>

    @Query("SELECT * FROM Feeds WHERE id = :id")
    suspend fun findFeed(id: Long): List<Feed>

    @Query("SELECT * FROM Feeds WHERE id IS :feedId")
    suspend fun loadFeed(feedId: Long): Feed?

    @Query("SELECT * FROM Feeds")
    fun loadFeeds(): List<Feed>

    @Query("SELECT * FROM Feeds WHERE tag IS :tag")
    suspend fun loadFeeds(tag: String): List<Feed>
}
