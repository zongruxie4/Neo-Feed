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

package com.saulhdev.feeder.data.db.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    tableName = "SyncActionQueue",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["remoteItemId"]),
    ]
)
data class SyncActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: String,
    val remoteItemId: Long,
    val actionType: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

object SyncActionType {
    const val MARK_READ = "MARK_READ"
    const val MARK_UNREAD = "MARK_UNREAD"
    const val STAR = "STAR"
    const val UNSTAR = "UNSTAR"
}
