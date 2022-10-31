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
import java.net.URL

class FeedRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedRepository")
    private val feedDao = NeoFeedDb.getInstance(context).feedDao()

    fun saveFeed(feed: Feed) {
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

    fun getAllFeeds(): List<Feed> {
        return feedDao.feedList()
    }

    fun deleteFeed(feed: Feed) {
        scope.launch {
            feedDao.delete(feed)
        }
    }
}