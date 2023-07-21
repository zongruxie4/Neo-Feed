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

package com.saulhdev.feeder.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.models.EditFeedViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.koin.java.KoinJavaComponent.inject

class EditFeedViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: ArticleRepository by inject(ArticleRepository::class.java)

    private val _viewState = MutableStateFlow(EditFeedViewState())
    val viewState: StateFlow<EditFeedViewState>
        get() = _viewState.asStateFlow()

    private var _feedId: MutableStateFlow<Long> = MutableStateFlow(
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

    init {
        viewModelScope.launch {
            val feed = repository.getFeed(_feedId.value).firstOrNull()
                ?: throw IllegalArgumentException("No feed with id ${_feedId.value}!")

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