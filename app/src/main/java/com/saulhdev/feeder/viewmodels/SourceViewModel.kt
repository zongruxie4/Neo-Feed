package com.saulhdev.feeder.viewmodels

import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.extensions.NeoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class SourceViewModel(
    private val articleRepo: ArticleRepository,
    private val feedsRepo: SourcesRepository,
) : NeoViewModel() {

    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    fun getFeedById(id: Long): Flow<Feed?> {
        return feedsRepo.getSourceById(id)
    }

    val allFeeds = feedsRepo.getAllSourcesFlow()
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
}