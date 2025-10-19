package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.utils.extensions.NeoViewModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SourceListViewModel(
    private val feedsRepo: SourcesRepository,
) : NeoViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val allFeeds = feedsRepo.getAllSourcesFlow()
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val allEnabledFeeds = feedsRepo.getEnabledSources()
        .stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val allTags = feedsRepo.getAllTagsFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val tagsFeedsMap = combine(allTags, allFeeds) { tags, feeds ->
        tags.plus("").associateWith { tag -> feeds.filter { it.tag.contains(tag) } }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    fun insertFeed(feed: Feed) {
        viewModelScope.launch {
            feedsRepo.insertSource(feed)
        }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch {
            feedsRepo.deleteFeed(feed)
        }
    }

    fun saveFeed(results: List<SearchResult>) {
        results.forEach { result ->
            if (result.isError) {
                return@forEach
            } else {
                val feed = Feed(
                    title = result.title,
                    description = result.description,
                    url = sloppyLinkToStrictURL(result.url),
                    feedImage = sloppyLinkToStrictURL(result.url)
                )
                insertFeed(feed)
            }
        }
    }
}