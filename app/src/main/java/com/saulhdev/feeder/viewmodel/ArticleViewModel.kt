/*
 * This file is part of Neo Feed
 * Copyright (c) 2025 Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.viewmodel

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class ArticleViewModel(
    private val articleRepo: ArticleRepository,
    private val feedsRepo: FeedRepository,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    fun articleById(id: Long) = articleRepo.getArticleById(id)
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            null
        )

    fun getFeedById(id: Long): Flow<Feed?> {
        return feedsRepo.getFeedById(id)
    }
}