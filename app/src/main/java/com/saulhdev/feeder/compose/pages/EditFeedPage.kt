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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ComposeSwitchView
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.preferenceGraph
import com.saulhdev.feeder.compose.util.interceptKey
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.models.EditFeedViewState
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.viewmodel.DIAwareViewModel
import com.saulhdev.feeder.viewmodel.EditFeedViewModel

@Composable
fun EditFeedPage(
    editFeedViewModel: EditFeedViewModel,
    feedId: Long = -1
) {
    val title = stringResource(id = R.string.edit_rss)
    editFeedViewModel.setFeedId(feedId)
    val viewState by editFeedViewModel.viewState.collectAsState()

    ViewWithActionBar(
        title = title,
        showBackButton = true,
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EditFeedView(
                viewState = viewState,
                feedTitle = editFeedViewModel::setTitle,
                feedUrl = editFeedViewModel::setUrl,
                feedFullTextByDefault = editFeedViewModel::setFullTextByDefault,
                feedIsEnabled = editFeedViewModel::setIsEnabled
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditFeedView(
    viewState: EditFeedViewState,
    feedTitle: (String) -> Unit,
    feedUrl: (String) -> Unit,
    feedFullTextByDefault: (Boolean) -> Unit,
    feedIsEnabled: (Boolean) -> Unit
) {
    val (focusTitle, focusTag) = createRefs()
    val focusManager = LocalFocusManager.current
    val repository = FeedRepository(LocalContext.current)

    Column {
        OutlinedTextField(
            value = viewState.url,
            onValueChange = feedUrl,
            label = {
                Text(stringResource(id = R.string.add_input_hint))
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusTitle.requestFocus()
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .interceptKey(Key.Enter) {
                    focusTitle.requestFocus()
                }
                .interceptKey(Key.Escape) {
                    focusManager.clearFocus()
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewState.title,
            onValueChange = feedTitle,
            label = {
                Text(stringResource(id = R.string.title))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusTag.requestFocus()
                }
            ),
            modifier = Modifier
                .focusRequester(focusTitle)
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .interceptKey(Key.Enter) {
                    focusTag.requestFocus()
                }
                .interceptKey(Key.Escape) {
                    focusManager.clearFocus()
                }
        )
        Spacer(modifier = Modifier.height(16.dp))

        ComposeSwitchView(
            title = stringResource(id = R.string.fetch_full_articles_by_default),
            isChecked = viewState.fullTextByDefault,
            onCheckedChange = feedFullTextByDefault,
            index = 0,
            groupSize = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        ComposeSwitchView(
            title = stringResource(id = R.string.source_enabled),
            isChecked = viewState.isEnabled,
            onCheckedChange = feedIsEnabled,
            index = 1,
            groupSize = 2
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {

            val navController = LocalNavController.current
            OutlinedButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(
                    text = stringResource(id = android.R.string.cancel)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    repository.updateFeed(
                        viewState.title,
                        sloppyLinkToStrictURL(viewState.url),
                        viewState.fullTextByDefault,
                        viewState.isEnabled
                    )
                    navController.popBackStack()
                }
            ) {
                Text(
                    text = stringResource(id = android.R.string.ok)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.editFeedGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{feedId}"),
            arguments = listOf(
                navArgument("feedId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val feedId = args.getLong("feedId")
            EditFeedPage(
                editFeedViewModel = backStackEntry.DIAwareViewModel(),
                feedId = feedId
            )
        }
    }
}