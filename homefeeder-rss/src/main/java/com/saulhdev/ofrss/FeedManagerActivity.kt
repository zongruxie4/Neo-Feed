/*
 * This file is part of Omega Feeder
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

package com.saulhdev.ofrss

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.saulhdev.ofrss.compose.ComposeBottomSheet
import com.saulhdev.ofrss.compose.FeedItem
import com.saulhdev.ofrss.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import ua.itaysonlab.hfrss.data.SavedFeedModel
import ua.itaysonlab.hfrss.pref.HFPluginPreferences

class FeedManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainFeedView()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainFeedView() {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val context = LocalContext.current

    val rssList = remember { mutableStateOf(HFPluginPreferences.parsedFeedList) }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            ComposeBottomSheet(
                onSaveAction = {
                    coroutineScope.launch{
                        var data: Channel? = null
                        withContext(Dispatchers.Default) {
                            val parser = Parser.Builder()
                                .okHttpClient(OkHttpClient())
                                .build()
                            try {
                                data = parser.getChannel(it)
                            } catch (_: Exception) {

                            }
                        }
                        data ?: run {
                            Toast.makeText(context, "URL is not a RSS feed!", Toast.LENGTH_LONG).show()
                            return@launch
                        }
                        val title = data!!.title ?: "Unknown"
                        val savedFeedModel = SavedFeedModel(title, data!!.description ?: "", it,data!!.image?.url ?: "")

                        HFPluginPreferences.add(savedFeedModel)
                        rssList.value = HFPluginPreferences.parsedFeedList
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                },
                onCloseAction = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.manager_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    backgroundColor = MaterialTheme.colorScheme.background
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(all = 16.dp),
                    onClick = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                }
            }
        ) {
            LazyColumn(modifier = Modifier.padding(paddingValues = it)) {
                items(rssList.value) { item ->
                    FeedItem(
                        feedTitle = item.name,
                        feedURL = item.feedUrl,
                        feedImage = item.feedImage,
                        description = item.description,
                        onRemoveAction = {
                            androidx.appcompat.app.AlertDialog.Builder(context).apply {
                                setTitle(R.string.remove_title)
                                setMessage(context.resources.getString(R.string.remove_desc, item.name))
                                setNeutralButton(R.string.remove_action_nope, null)
                                setPositiveButton(R.string.remove_action_yes) { _, _ ->
                                    HFPluginPreferences.remove(item)
                                    rssList.value = HFPluginPreferences.parsedFeedList
                                }
                            }.show()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MainFeedViewPreview() {
    MainFeedView()
}
