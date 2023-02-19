package com.saulhdev.feeder.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.models.EditFeedViewState
import com.saulhdev.feeder.models.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class EditFeedViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    private val _feedId: MutableStateFlow<Long> = MutableStateFlow(
        state["feedId"] ?: -1
    )

    fun setFeedId(value: Long) {
        state["feedId"] = value
        _feedId.update { value }
    }

    private val _url: MutableStateFlow<String> = MutableStateFlow(
        state["feedUrl"] ?: ""
    )

    fun setUrl(value: String) {
        state["feedUrl"] = value
        _url.update { value }
    }

    private val _title: MutableStateFlow<String> = MutableStateFlow(
        state["feedTitle"] ?: ""
    )

    fun setTitle(value: String) {
        state["feedTitle"] = value
        _title.update { value }
    }

    private val _fullTextByDefault: MutableStateFlow<Boolean> = MutableStateFlow(
        state["fullTextByDefault"] ?: false
    )

    fun setFullTextByDefault(value: Boolean) {
        state["fullTextByDefault"] = value
        _fullTextByDefault.update { value }
    }

    private val _isEnabled: MutableStateFlow<Boolean> = MutableStateFlow(
        state["isEnabled"] ?: true
    )

    fun setIsEnabled(value: Boolean) {
        state["isEnabled"] = value
        _isEnabled.update { value }
    }

    private val _viewState = MutableStateFlow(EditFeedViewState())
    val viewState: StateFlow<EditFeedViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            // Set initial state in case state is empty
            val feed = repository.getFeed(_feedId.value)
                ?: throw IllegalArgumentException("No feed with id $_feedId!")

            if (!state.contains("feedUrl")) {
                setUrl(feed.url.toString())
            }

            if (!state.contains("feedTitle")) {
                setTitle(feed.title)
            }
            if (!state.contains("fullTextByDefault")) {
                setFullTextByDefault(feed.fullTextByDefault)
            }
            if (!state.contains("isEnabled")) {
                setIsEnabled(feed.isEnabled)
            }

            combine(
                _title,
                _url,
                _fullTextByDefault,
                _isEnabled
            ) { params: Array<Any> ->
                EditFeedViewState(
                    title = params[0] as String,
                    url = params[1] as String,
                    fullTextByDefault = params[2] as Boolean,
                    isEnabled = params[3] as Boolean
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}