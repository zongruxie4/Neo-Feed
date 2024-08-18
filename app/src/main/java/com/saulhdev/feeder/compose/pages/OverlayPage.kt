/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.compose.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ArticleItem
import com.saulhdev.feeder.compose.components.BookmarkItem
import com.saulhdev.feeder.compose.components.PullToRefreshLazyColumn
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.NavRoute
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.Bookmarks
import com.saulhdev.feeder.icon.phosphor.CaretUp
import com.saulhdev.feeder.icon.phosphor.GearSix
import com.saulhdev.feeder.icon.phosphor.Nut
import com.saulhdev.feeder.icon.phosphor.Power
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sync.SyncRestClient
import com.saulhdev.feeder.utils.launchView
import com.saulhdev.feeder.utils.openLinkInCustomTab
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun OverlayPage(isOverlay: Boolean = false) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val prefs = FeedPreferences.getInstance(context)

    val articles: SyncRestClient by inject(SyncRestClient::class.java)
    val repository: ArticleRepository by inject(ArticleRepository::class.java)
    val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    val feedList by repository.getFeedArticles()
        .mapLatest { articles ->
            (if (prefs.removeDuplicates.getValue()) articles.distinctBy { it.content.link }
            else articles)
                .sortedByDescending { it.time }
        }
        .collectAsState(initial = emptyList())
    val bookmarked = repository.getBookmarkedArticlesMap().collectAsState(initial = emptyMap())

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var isRefreshing by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showFAB by remember { derivedStateOf { listState.firstVisibleItemIndex > 4 } }

    LaunchedEffect(key1 = null) {
        isRefreshing = true
    }

    LaunchedEffect(key1 = null) {
        scope.launch {
            repository.isSyncing
                .collectLatest {
                    if (isRefreshing && !it) isRefreshing = false
                }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    Surface(
                        color = if (showBookmarks) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            showBookmarks = !showBookmarks
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(8.dp),
                            imageVector = Phosphor.Bookmarks,
                            contentDescription = stringResource(id = R.string.title_bookmarks),
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .size(size = 40.dp)
                            .clip(CircleShape),
                        onClick = {
                            showMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Phosphor.Nut,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.action_reload))
                                },
                                onClick = {
                                    showMenu = false
                                    isRefreshing = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                    )
                                }
                            )
                            HorizontalDivider()

                            if (isOverlay) DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.title_settings))
                                },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(NavRoute.Main(1))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Phosphor.GearSix,
                                        contentDescription = null,
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.action_restart))
                                },
                                onClick = {
                                    showMenu = false
                                    NFApplication.instance.restart(false)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Phosphor.Power,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFAB,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        scope.launch {
                            withContext(AndroidUiDispatcher.Main) {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Phosphor.CaretUp,
                        contentDescription = null,
                    )
                }
            }
        }
    ) { paddingValues ->
        if (showBookmarks) LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = paddingValues.calculateTopPadding()
            )
        ) {
            items(bookmarked.value.entries.toList(), key = { it.key.id }) { item ->
                BookmarkItem(
                    article = item.key,
                    feed = item.value,
                    onClickAction = { article ->
                        if (prefs.openInBrowser.getValue()) {
                            context.launchView(article.link ?: "")
                        } else {
                            scope.launch {
                                if (prefs.offlineReader.getValue()) {
                                    context.startActivity(
                                        MainActivity.navigateIntent(
                                            context,
                                            "${Routes.ARTICLE_VIEW}/${article.id}"
                                        )
                                    )
                                } else {
                                    openLinkInCustomTab(
                                        context,
                                        article.link!!
                                    )
                                }
                            }
                        }
                        scope.launch {
                            repository.unpinArticle(article.id)
                        }
                    },
                    onRemoveAction = {
                        scope.launch {
                            repository.bookmarkArticle(it.id, false)
                        }
                    }
                )
            }
        }
        else PullToRefreshLazyColumn(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                refreshFeed(articles, scope)
            },
            listState = listState,
            content = {
                items(feedList, key = { it.id }) { item ->
                    ArticleItem(
                        article = item,
                        onBookmark = {
                            repository.bookmarkArticle(item.id, it)
                        },
                    ) {
                        if (prefs.openInBrowser.getValue()) {
                            context.launchView(item.content.link)
                        } else {
                            if (prefs.offlineReader.getValue()) {
                                context.startActivity(
                                    MainActivity.navigateIntent(
                                        context,
                                        "${Routes.ARTICLE_VIEW}/${item.id}"
                                    )
                                )
                            } else {
                                openLinkInCustomTab(
                                    context,
                                    item.content.link
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding()
            )
        )
    }
}

fun refreshFeed(
    articles: SyncRestClient,
    scope: CoroutineScope
) {
    scope.launch {
        articles.syncAllFeeds()
    }
}
