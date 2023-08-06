package com.saulhdev.feeder.compose.pages

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.OverflowMenu
import com.saulhdev.feeder.compose.components.ViewWithActionBar

@Composable
fun OverlayPage() {
    /*val context = LocalContext.current
    val navController = LocalNavController.current

    val repository = ArticleRepository(context)
    val articles = SyncRestClient(context)
    val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    val feedList = remember { arrayListOf<FeedItem>() }
*/
    LaunchedEffect(key1 = null) {
        /*scope.launch {
            val feeds = repository.getAllFeeds()
            for (feed in feeds) {
                articles.getArticleList(feed)
            }

            PluginConnector.getFeedAsItLoads(0, { feed ->
                feedList.addAll(feed)
            }) {
                feedList.sortByDescending { it.time }
            }
        }*/
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
                        //navController.navigate(Routes.SETTINGS)
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
        /*LazyColumn(
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

        }*/
    }
}
