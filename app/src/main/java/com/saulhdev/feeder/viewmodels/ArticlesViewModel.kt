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
import com.saulhdev.feeder.data.entity.FeedItem
import com.saulhdev.feeder.data.entity.SORT_CHRONOLOGICAL
import com.saulhdev.feeder.data.entity.SORT_SOURCE
import com.saulhdev.feeder.data.entity.SORT_TITLE
import com.saulhdev.feeder.data.entity.SortFilterModel
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.FeedRepository
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ArticlesViewModel(
    private val articleRepo: ArticleRepository,
    feedsRepo: FeedRepository,
    private val prefs: FeedPreferences,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val activeFeeds = feedsRepo.getEnabledFeeds()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val prefSortFilter = combine(
        prefs.sortingFilter.get(),
        prefs.sortingAsc.get(),
        prefs.sourcesFilter.get(),
    ) { sort, sortAsc, sources ->
        SortFilterModel(sort, sortAsc, sources)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SortFilterModel()
        )

    val notModifiedFilter = prefSortFilter.map {
        it == SortFilterModel()
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            true
        )

    val articlesList: StateFlow<List<FeedItem>> = combine(
        articleRepo.getFeedArticles(),
        prefSortFilter,
        prefs.removeDuplicates.get(),
    ) { articles, sfm, removeDuplicate ->
        (if (removeDuplicate) articles.distinctBy { it.content.link }
        else articles)
            .let {
                if (sfm.sourcesFilter.isEmpty()) it
                else it.filterNot { sfm.sourcesFilter.contains(it.content.source.id) }
            }
            .let {
                when {
                    sfm.sort == SORT_CHRONOLOGICAL && !sfm.sortAsc
                         -> it.sortedByDescending { it.time } // default

                    sfm.sort == SORT_TITLE && sfm.sortAsc
                         -> it.sortedBy { it.content.title }

                    sfm.sort == SORT_TITLE && !sfm.sortAsc
                         -> it.sortedByDescending { it.content.title }

                    sfm.sort == SORT_SOURCE && sfm.sortAsc
                         -> it.sortedBy { it.title }

                    sfm.sort == SORT_SOURCE && !sfm.sortAsc
                         -> it.sortedByDescending { it.title }

                    // sfm.sort == SORT_CHRONOLOGICAL && sfm.sortAsc
                    else -> it.sortedBy { it.time }
                }
            }
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val bookmarked = combine(
        articleRepo.getBookmarkedArticlesMap(),
        prefSortFilter,
    ) { articles, sfm ->
        (if (sfm.sourcesFilter.isEmpty()) articles.entries
        else articles.entries.filterNot { sfm.sourcesFilter.contains(it.value.id.toString()) })
            .let {
                when {
                    sfm.sort == SORT_CHRONOLOGICAL && !sfm.sortAsc
                         -> it.sortedByDescending { it.key.pubDate } // default

                    sfm.sort == SORT_TITLE && sfm.sortAsc
                         -> it.sortedBy { it.key.title }

                    sfm.sort == SORT_TITLE && !sfm.sortAsc
                         -> it.sortedByDescending { it.key.title }

                    sfm.sort == SORT_SOURCE && sfm.sortAsc
                         -> it.sortedBy { it.value.title }

                    sfm.sort == SORT_SOURCE && !sfm.sortAsc
                         -> it.sortedByDescending { it.value.title }

                    // sfm.sort == SORT_CHRONOLOGICAL && sfm.sortAsc
                    else -> it.sortedBy { it.key.pubDate }
                }
            }

    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val isSyncing = feedsRepo.isSyncing

    fun unpinArticle(id: Long) {
        viewModelScope.launch {
            articleRepo.unpinArticle(id)
        }
    }

    fun bookmarkArticle(id: Long, boolean: Boolean) {
        viewModelScope.launch {
            articleRepo.bookmarkArticle(id, boolean)
        }
    }
}