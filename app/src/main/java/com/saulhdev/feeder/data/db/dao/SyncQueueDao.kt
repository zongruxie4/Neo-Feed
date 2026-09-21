/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

package com.saulhdev.feeder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saulhdev.feeder.data.db.models.SyncActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: SyncActionEntity): Long

    @Query("SELECT * FROM SyncActionQueue WHERE accountId = :accountId ORDER BY createdAt ASC")
    suspend fun getPendingActions(accountId: String): List<SyncActionEntity>

    @Query("SELECT * FROM SyncActionQueue ORDER BY createdAt ASC")
    suspend fun getAllPendingActions(): List<SyncActionEntity>

    @Query("SELECT COUNT(*) FROM SyncActionQueue WHERE accountId = :accountId")
    fun getPendingCountFlow(accountId: String): Flow<Int>

    @Query("DELETE FROM SyncActionQueue WHERE id IN (:ids)")
    suspend fun deleteActions(ids: List<Long>): Int

    @Query("DELETE FROM SyncActionQueue WHERE accountId = :accountId")
    suspend fun clearActionsForAccount(accountId: String): Int
}
