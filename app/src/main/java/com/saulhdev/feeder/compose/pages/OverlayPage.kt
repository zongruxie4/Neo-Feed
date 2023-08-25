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

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ArticleItem
import com.saulhdev.feeder.compose.components.OverflowMenu
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.icon.Phosphor
import com.saulhdev.feeder.compose.icon.phosphor.CloudArrowDown
import com.saulhdev.feeder.compose.icon.phosphor.CloudArrowUp
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.models.exportOpml
import com.saulhdev.feeder.models.importOpml
import com.saulhdev.feeder.plugin.PluginConnector
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sync.SyncRestClient
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.launchView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import java.time.LocalDateTime


@Composable
fun OverlayPage() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val di = LocalDI.current
    val localTime = LocalDateTime.now().toString().replace(":", "_").substring(0, 19)

    val articles = SyncRestClient(context)
    val repository = ArticleRepository(context)
    val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    val feedList: SnapshotStateList<FeedItem> = remember { mutableStateListOf() }
    val prefs = FeedPreferences.getInstance(context)

    LaunchedEffect(key1 = true) {
        scope.launch {
            val feeds = repository.getAllFeeds()
            for (feed in feeds) {
                articles.getArticleList(feed)
            }

            PluginConnector.getFeedAsItLoads(0, { feed ->
                feedList.addAll(feed)
            }) {
                feedList.sortedByDescending { it.time }
            }
        }
    }

    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                importOpml(di, uri)
            }
        }
    }

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml")
    ) { uri ->
        if (uri != null) {
            val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
            applicationCoroutineScope.launch {
                exportOpml(di, uri)
            }
        }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.app_name),
        floatingActionButton = { },
        showBackButton = false,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.action_reload))
                    },
                    onClick = {
                        hideMenu()
                        //refreshFeed()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_replay_24),
                            contentDescription = null,
                        )
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.sources_import_opml))
                    },
                    onClick = {
                        hideMenu()
                        opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*"))
                    },
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowDown,
                            contentDescription = null,
                        )
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.sources_export_opml))
                    },
                    onClick = {
                        hideMenu()
                        opmlExporter.launch("NF-${localTime}.opml")
                    },
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowUp,
                            contentDescription = null,
                        )
                    }
                )
                Divider()

                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.title_settings))
                    },
                    onClick = {
                        hideMenu()
                        navController.navigate(Routes.SETTINGS)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings_outline_28),
                            contentDescription = null,
                        )
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.action_restart))
                    },
                    onClick = {
                        hideMenu()
                        NFApplication.instance.restart(false)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_restart),
                            contentDescription = null,
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 8.dp,
                    end = 8.dp
                )
        ) {
            items(feedList.size) { index ->
                val item = feedList[index]
                ArticleItem(
                    article = item,
                    repository = repository
                ) {
                    if (prefs.openInBrowser.getValue()) {
                        context.launchView(item.content.link)
                    } else {
                        if (prefs.offlineReader.getValue()) {
                            navController.navigate("/${Routes.ARTICLE_VIEW}/${item.id}/")
                        } else {
                            openLinkInCustomTab(
                                context,
                                item.content.link
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun openLinkInCustomTab(
    context: Context,
    link: String
): Boolean {
    @ColorInt val colorPrimaryLight =
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    @ColorInt val colorPrimaryDark =
        ContextCompat.getColor(context, R.color.md_theme_light_primary)
    try {
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setSecondaryToolbarColor(Color.BLACK)
            .setToolbarColor(colorPrimaryLight)
            .build()

        val intent = CustomTabsIntent.Builder()
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorPrimaryLight)
                    .build()
            )
            .setColorSchemeParams(
                CustomTabsIntent.COLOR_SCHEME_DARK, CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorPrimaryDark)
                    .build()
            )
            .build()
        intent.launchUrl(context, Uri.parse(link))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}
