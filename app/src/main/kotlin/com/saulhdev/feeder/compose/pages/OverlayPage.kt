package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ArticleItem
import com.saulhdev.feeder.compose.components.OverflowMenu
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.plugin.PluginConnector
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sync.SyncRestClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@Composable
fun OverlayPage() {
    val context = LocalContext.current
    val navController = LocalNavController.current

    val repository = ArticleRepository(context)
    val articles = SyncRestClient(context)
    val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    val feedList = remember { arrayListOf<FeedItem>() }

    LaunchedEffect(key1 = null) {
        scope.launch {
            val feeds = repository.getAllFeeds()
            for (feed in feeds) {
                articles.getArticleList(feed)
            }

            PluginConnector.getFeedAsItLoads(0, { feed ->
                feedList.addAll(feed)
            }) {
                feedList.sortByDescending { it.time }
            }
        }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.app_name),
        floatingActionButton = { },
        actions = {
            OverflowMenu {
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
                .background(MaterialTheme.colorScheme.primary)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
                )
        ) {
            items(feedList.size) { index ->
                val article = feedList[index]
                ArticleItem(article = article, repository = repository)
            }

        }
    }
}
