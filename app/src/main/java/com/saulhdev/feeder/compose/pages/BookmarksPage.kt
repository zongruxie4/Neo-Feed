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

package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.BookmarkItem
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.utils.launchView
import com.saulhdev.feeder.utils.openLinkInCustomTab
import kotlinx.coroutines.launch

@Composable
fun BookmarksPage() {
    val context = LocalContext.current
    val prefs = FeedPreferences.getInstance(context)
    val scope = rememberCoroutineScope()
    val repository = ArticleRepository(context)
    val bookmarked = repository.getBookmarkedArticlesMap().collectAsState(initial = emptyMap())

    ViewWithActionBar(
        title = stringResource(id = R.string.title_bookmarks),
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 8.dp,
                end = 8.dp,
            ),
        ) {
            items(bookmarked.value.entries.toList()) { item ->
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
                                        MainActivity.createIntent(
                                            context,
                                            "article_page/${article.id}/"
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
    }
}
