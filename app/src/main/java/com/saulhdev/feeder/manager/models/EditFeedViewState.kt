package com.saulhdev.feeder.manager.models

import androidx.compose.runtime.Immutable

@Immutable
data class EditFeedViewState(
    val title: String = "",
    val url: String = "",
    val fullTextByDefault: Boolean = true,
    val isEnabled: Boolean = true,
)