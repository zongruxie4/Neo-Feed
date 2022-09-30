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

import android.webkit.URLUtil.isValidUrl
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.util.interceptKey
import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.preference.FeedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddFeedPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val title = stringResource(id = R.string.add_rss)

    var currentlySearching by rememberSaveable {
        mutableStateOf(false)
    }
    var results by rememberSaveable {
        mutableStateOf(listOf<SavedFeedModel>())
    }
    var errors by rememberSaveable {
        mutableStateOf(listOf<SavedFeedModel>())
    }
    val prefs = FeedPreferences(context)
    val feedList = prefs.feedList.onGetValue().map { SavedFeedModel(JSONObject(it)) }
    val rssList = remember { mutableStateOf(feedList) }
    var rssURL by remember { mutableStateOf("") }

    ViewWithActionBar(title = title) { paddingValues ->
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val (focusTitle, focusTag) = createRefs()

        var feedUrl by remember { mutableStateOf("") }
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isNotValidUrl by remember(feedUrl) {
                derivedStateOf {
                    feedUrl.isNotEmpty() && isNotValidUrl(feedUrl)
                }
            }
            val isValidUrl by remember(feedUrl) {
                derivedStateOf {
                    isValidUrl(feedUrl)
                }
            }

            OutlinedTextField(
                value = feedUrl,
                onValueChange = { feedUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .interceptKey(Key.Enter) {
                        focusTitle.requestFocus()
                    }
                    .interceptKey(Key.Escape) {
                        focusManager.clearFocus()
                    },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                    textColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (isValidUrl) {
                            //onSearch(sloppyLinkToStrictURLNoThrows(feedUrl))
                            keyboardController?.hide()
                        }
                    }
                ),
                shape = MaterialTheme.shapes.medium,
                label = { Text(text = stringResource(id = R.string.add_input_hint)) }
            )

            OutlinedButton(
                enabled = isValidUrl,
                onClick = {
                    results = emptyList()
                    errors = emptyList()
                    currentlySearching = true
                    coroutineScope.launch {
                        var data: Channel? = null
                        withContext(Dispatchers.Default) {
                            val parser = Parser.Builder()
                                .okHttpClient(OkHttpClient())
                                .build()
                            try {
                                data = parser.getChannel(rssURL)
                            } catch (_: Exception) {

                            }
                        }
                        data ?: run {
                            Toast.makeText(context, "URL is not a RSS feed!", Toast.LENGTH_LONG)
                                .show()
                            return@launch
                        }
                        val feedTitle = data!!.title ?: "Unknown"
                        val savedFeedModel = SavedFeedModel(
                            feedTitle,
                            data!!.description ?: "",
                            rssURL,
                            data!!.image?.url ?: ""
                        )
                        rssList.value = rssList.value + savedFeedModel
                        rssURL = ""
                        val stringSet = rssList.value.map {
                            it.asJson().toString()
                        }.toSet()
                        prefs.feedList.onSetValue(stringSet)
                    }
                }
            ) {
                Text(
                    stringResource(android.R.string.search_go)
                )
            }
        }
    }
}

private fun isNotValidUrl(url: String) = !isValidUrl(url)

@Preview
@Composable
fun AddFeedPagePreview() {
    AddFeedPage()
}