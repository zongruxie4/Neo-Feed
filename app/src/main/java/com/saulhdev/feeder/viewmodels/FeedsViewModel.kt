/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.repository.FeedRepository
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class FeedsViewModel(private val feedsRepo: FeedRepository) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val allFeeds = feedsRepo.getAllFeeds()
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    fun insertFeed(feed: Feed) {
        viewModelScope.launch {
            feedsRepo.insertFeed(feed)
        }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch {
            feedsRepo.deleteFeed(feed)
        }
    }
}