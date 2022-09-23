package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.saulhdev.feeder.compose.components.TopBar

@Composable
fun WebViewPage(url: String) {
    val state = rememberWebViewState("url")
    val title = remember { mutableStateOf(url) }

    Scaffold(
        topBar = { TopBar(title) },
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.padding(it)) {
            WebView(
                state = state,
                onCreated = { it.settings.javaScriptEnabled = true }
            )
        }
    }
}
