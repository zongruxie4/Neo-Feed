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

package com.saulhdev.feeder.data.source

import android.content.Context
import com.saulhdev.feeder.data.account.AccountType

data class SyncResult(
    val success: Boolean,
    val itemsCount: Int = 0,
    val errorMessage: String? = null,
)

interface NewsSource {
    val accountId: String
    val accountType: AccountType
    val displayName: String

    suspend fun sync(context: Context, forceNetwork: Boolean): SyncResult
    suspend fun syncPendingActions(): Boolean = true
}
