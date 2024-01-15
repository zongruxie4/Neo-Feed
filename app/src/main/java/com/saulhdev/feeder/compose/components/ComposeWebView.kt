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

package com.saulhdev.feeder.compose.components

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import com.saulhdev.feeder.compose.navigation.preferenceGraph
import com.saulhdev.feeder.utils.urlDecode

@Composable
fun ComposeWebView(
    pageUrl: String
) {
    val url by remember { mutableStateOf(pageUrl) }
    val state = rememberWebViewState(url = url)
    val navigator = rememberWebViewNavigator()

    val loadingState = state.loadingState
    val title = remember { mutableStateOf("Neo Feed") }
    val subTitle = remember { mutableStateOf("Neo Feed") }

    val navController = rememberNavController()
    val activity = (LocalContext.current as? Activity)

    BackHandler {
        if (navController.currentBackStackEntry?.destination?.route == null) {
            activity?.finish()
        } else {
            navController.popBackStack()
        }
    }

    ViewWithActionBar(
        title = title.value,
        titleSize = 16.sp,
        subTitle = subTitle.value,
        showBackButton = true,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                ),
        ) {
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = { loadingState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                )
            } else {
                title.value = state.pageTitle ?: "Neo Feed"
                val currentUrl = state.content.getCurrentUrl() ?: "Neo Feed"
                if (currentUrl != "Neo Feed" && state.content.getCurrentUrl() != null) {
                    subTitle.value = Uri.parse(currentUrl).host!!
                }
            }


            val webClient = remember {
                object : AccompanistWebViewClient() {
                }
            }

            WebView(
                state = state,
                modifier = Modifier.weight(1f),
                navigator = navigator,
                onCreated = { webView ->
                    webView.settings.let {
                        it.javaScriptEnabled = true
                        it.domStorageEnabled = true
                        it.setSupportZoom(true)
                        it.builtInZoomControls = true
                        it.displayZoomControls = false
                    }
                },
                client = webClient
            )

        }
    }
}

fun NavGraphBuilder.webViewerGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{url}"),
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")?.urlDecode()
            if (url != null) {
                ComposeWebView(pageUrl = url)
            }
        }
    }
}