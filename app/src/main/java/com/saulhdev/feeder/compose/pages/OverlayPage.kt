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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ArticleItem
import com.saulhdev.feeder.compose.components.PullToRefreshLazyColumn
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.CaretUp
import com.saulhdev.feeder.icon.phosphor.GearSix
import com.saulhdev.feeder.icon.phosphor.Nut
import com.saulhdev.feeder.icon.phosphor.Power
import com.saulhdev.feeder.plugin.PluginConnector
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sync.SyncRestClient
import com.saulhdev.feeder.utils.launchView
import com.saulhdev.feeder.utils.openLinkInCustomTab
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.java.KoinJavaComponent.inject
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPage(navController: NavController = LocalNavController.current) {
    val context = LocalContext.current
    val localTime = LocalDateTime.now().toString().replace(":", "_").substring(0, 19)

    val articles : SyncRestClient by inject(SyncRestClient::class.java)
    val repository: ArticleRepository by inject(ArticleRepository::class.java)
    val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    val feedList: MutableList<FeedItem> = remember { mutableStateListOf() }

    val prefs = FeedPreferences.getInstance(context)

    LaunchedEffect(key1 = null) {
        refreshFeed(repository, articles, scope) {
            feedList.clear()
            PluginConnector.getFeedAsItLoads(0, { feed ->
                feedList.addAll(feed)
            }, {

                feedList.sortByDescending { it.time }
            })
        }
    }

    /*val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                importOpml(uri)
            }
        }
    }

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                exportOpml(uri)
            }
        }
    }*/

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var isRefreshing by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showFAB by remember { derivedStateOf { listState.firstVisibleItemIndex > 4 } }

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
                            /*
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.sources_import_opml))
                                },
                                onClick = {
                                    showMenu = false
                                    opmlImporter.launch(
                                        arrayOf(
                                            "text/plain",
                                            "text/xml",
                                            "text/opml",
                                            ""
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Phosphor.CloudArrowDown,
                                        contentDescription = null,
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.sources_export_opml))
                                },
                                onClick = {
                                    showMenu = false
                                    opmlExporter.launch("NF-${localTime}.opml")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Phosphor.CloudArrowUp,
                                        contentDescription = null,
                                    )
                                }
                            )
                            HorizontalDivider()
                            */

                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = R.string.title_settings))
                                },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Routes.SETTINGS)
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
        PullToRefreshLazyColumn(
            items = feedList,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                refreshFeed(repository, articles, scope) {
                    feedList.clear()
                    PluginConnector.getFeedAsItLoads(0, { feed ->
                        feedList.addAll(feed)
                    }, {
                        feedList.sortByDescending { it.time }
                        isRefreshing = false
                    })
                }
            },
            listState = listState,
            content = { item ->
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
                            //navController.navigate("/${Routes.ARTICLE_VIEW}/${item.id}/")
                            context.startActivity(
                                MainActivity.createIntent(
                                    context,
                                    "${Routes.ARTICLE_VIEW}/${item.id}/"
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
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

fun refreshFeed(
    repository: ArticleRepository,
    articles: SyncRestClient,
    scope: CoroutineScope,
    callback: () -> Unit
) {
    scope.launch {
        val feeds = repository.getAllFeeds()
        for (feed in feeds) {
            articles.getArticleList(feed)
        }
    }
    callback.invoke()
}
