/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
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
import com.saulhdev.feeder.data.db.models.Article
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ArticleViewModel(
    private val articleRepo: ArticleRepository,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    private val articleId: MutableStateFlow<String> = MutableStateFlow("")

    fun setArticleId(value: String) {
        articleId.update { value }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val articleState = articleId.flatMapLatest {
        articleRepo.getArticleById(it)
    }.mapLatest {
        ArticlePageState(
            it,
            it?.bookmarked ?: false
        )
    }
        .stateIn(
            ioScope,
            SharingStarted.Lazily,
            ArticlePageState()
        )

    fun unpinArticle(id: String) {
        viewModelScope.launch {
            articleRepo.unpinArticle(id)
        }
    }

    fun bookmarkArticle(id: String, boolean: Boolean) {
        viewModelScope.launch {
            articleRepo.bookmarkArticle(id, boolean)
        }
    }
}

data class ArticlePageState(
    val article: Article? = null,
    val isBookmarked: Boolean = false,
)