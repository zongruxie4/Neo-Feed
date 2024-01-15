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

package com.saulhdev.feeder.sync

import android.content.Context
import com.saulhdev.feeder.db.models.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRestClient(val context: Context) {
    suspend fun getArticleList(feed: Feed) {
        withContext(Dispatchers.IO) {
            requestFeedSync(feed.id)
        }
    }
}