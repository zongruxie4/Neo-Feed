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
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.db.models.FeedItem
import com.saulhdev.feeder.data.entity.SORT_CHRONOLOGICAL
import com.saulhdev.feeder.data.entity.SORT_SOURCE
import com.saulhdev.feeder.data.entity.SORT_TITLE
import com.saulhdev.feeder.data.entity.SortFilterModel
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ArticleViewModel(
    private val articleRepo: ArticleRepository,
    feedsRepo: SourcesRepository,
    val prefs: FeedPreferences,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val prefSortFilter = combine(
        prefs.sortingFilter.get(),
        prefs.sortingAsc.get(),
        prefs.sourcesFilter.get(),
        prefs.tagsFilter.get()
    ) { sort, sortAsc, sources, tags ->
        SortFilterModel(sort, sortAsc, sources, tags)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SortFilterModel()
        )

    fun articleById(id: String) = articleRepo.getArticleById(id)
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            null
        )

    val notModifiedFilter = prefSortFilter.map {
        it == SortFilterModel()
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            true
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tagsArticles: Flow<List<FeedItem>> = prefs.tagsFilter.get()
        .flatMapLatest { tags ->
            articleRepo.getFeedItemsByTags(tags)
        }

    val activeFeeds = feedsRepo.getEnabledSources()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val allArticles: Flow<List<FeedItem>> = articleRepo.getEnabledFeedItems()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles = prefs.tagsFilter.get()
        .flatMapLatest { tags ->
            if (tags.any()) tagsArticles
            else allArticles
        }

    val articlesList: StateFlow<List<FeedItem>> = combine(
        articles,
        prefSortFilter,
        prefs.removeDuplicates.get(),
    ) { articles, sfm, removeDuplicate ->
        (if (removeDuplicate) articles.distinctBy { it.link }
        else articles)
            .let {
                if (sfm.sourcesFilter.isEmpty()) it
                else it.filterNot { it.sourceId in sfm.sourcesFilter }

                if (sfm.tagsFilter.isEmpty()) it
                else it.filterNot { it.feedTag in sfm.tagsFilter }
            }
            .let {
                when {
                    sfm.sort == SORT_CHRONOLOGICAL && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::timeMillis) // default

                    sfm.sort == SORT_TITLE && sfm.sortAsc
                         -> it.sortedBy(FeedItem::contentTitle)

                    sfm.sort == SORT_TITLE && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::contentTitle)

                    sfm.sort == SORT_SOURCE && sfm.sortAsc
                         -> it.sortedBy(FeedItem::displayTitle)

                    sfm.sort == SORT_SOURCE && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::displayTitle)

                    else -> it.sortedBy(FeedItem::timeMillis)
                }
            }
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val bookmarkedArticles: Flow<List<FeedItem>> = articleRepo.getBookmarkedFeedItems()
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val bookmarkedArticlesList: StateFlow<List<FeedItem>> = combine(
        bookmarkedArticles,
        prefSortFilter,
        prefs.removeDuplicates.get(),
    ) { articles, sfm, removeDuplicate ->
        articles
            .let {
                if (removeDuplicate) it.distinctBy(FeedItem::link)
                else it
            }
            .let {
                if (sfm.sourcesFilter.isEmpty()) it
                else it.filterNot { sfm.sourcesFilter.contains(it.sourceId) }
            }
            .let {
                when {
                    sfm.sort == SORT_CHRONOLOGICAL && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::timeMillis) // default

                    sfm.sort == SORT_TITLE && sfm.sortAsc
                         -> it.sortedBy(FeedItem::contentTitle)

                    sfm.sort == SORT_TITLE && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::contentTitle)

                    sfm.sort == SORT_SOURCE && sfm.sortAsc
                         -> it.sortedBy(FeedItem::displayTitle)

                    sfm.sort == SORT_SOURCE && !sfm.sortAsc
                         -> it.sortedByDescending(FeedItem::displayTitle)

                    else -> it.sortedBy(FeedItem::timeMillis)
                }
            }
    }
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val isSyncing = feedsRepo.isSyncing

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