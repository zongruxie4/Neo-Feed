package com.saulhdev.feeder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SourcesViewModel(val repositoryDao: FeedDao) : ViewModel() {
    private val _repositories = MutableStateFlow<List<Feed>>(emptyList())
    val feedSources = _repositories.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDao.getAllFeeds().collectLatest {
                _repositories.emit(it)
            }
        }
    }

    //Get feed by Id
    fun getFeedById(id: Long) = repositoryDao.getFeedById(id)

    class Factory(private val repoDao: FeedDao) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SourcesViewModel::class.java)) {
                return SourcesViewModel(repoDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}