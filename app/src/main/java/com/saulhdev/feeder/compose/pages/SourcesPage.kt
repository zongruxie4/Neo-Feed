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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.FeedItem
import com.saulhdev.feeder.compose.components.OverflowMenu
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.components.dialog.ActionsDialogUI
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.SourceRepository
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.CloudArrowDown
import com.saulhdev.feeder.icon.phosphor.CloudArrowUp
import com.saulhdev.feeder.models.exportOpml
import com.saulhdev.feeder.models.importOpml
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.time.LocalDateTime

@Composable
fun SourcesPage() {
    val context = LocalContext.current

    val navController = LocalNavController.current
    val repository = SourceRepository(context)
    val localTime = LocalDateTime.now().toString().replace(":", "_").substring(0, 19)
    val coroutineScope: ApplicationCoroutineScope by inject(ApplicationCoroutineScope::class.java)

    val opmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                exportOpml(uri)
            }
        }
    }

    val opmlImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                importOpml(uri)
            }
        }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.title_sources),
        showBackButton = false,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("/${Routes.ADD_FEED}/")
                },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_feed),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowDown,
                            contentDescription = stringResource(id = R.string.sources_import_opml),
                        )
                    },
                    onClick = {
                        hideMenu()
                        opmlImporter.launch(arrayOf("text/plain", "text/xml", "text/opml", "*/*"))
                    },
                    text = { Text(text = stringResource(id = R.string.sources_import_opml)) }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowUp,
                            contentDescription = stringResource(id = R.string.sources_export_opml),
                        )
                    },
                    onClick = {
                        hideMenu()
                        opmlExporter.launch("NF-${localTime}.opml")
                    },
                    text = { Text(text = stringResource(id = R.string.sources_export_opml)) }
                )
            }
        }
    ) { paddingValues ->
        val showDialog = remember { mutableStateOf(false) }
        val list: State<List<Feed>> =
            repository.getAllFeeds().collectAsState(initial = listOf())
        val removeItem: MutableState<Feed?> =
            remember { mutableStateOf(list.value.firstOrNull()) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list.value, key = { it.id }) { item ->
                FeedItem(
                    feed = item,
                    onClickAction = {
                        navController.navigate(
                            "/${Routes.EDIT_FEED}/${item.id}/"
                        )
                    },
                    onRemoveAction = {
                        removeItem.value = item
                        showDialog.value = true
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        if (showDialog.value) {
            Dialog(
                onDismissRequest = { showDialog.value = false },
                DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                ActionsDialogUI(
                    titleText = stringResource(id = R.string.remove_title),
                    messageText = stringResource(
                        id = R.string.remove_desc,
                        removeItem.value!!.title
                    ),
                    openDialogCustom = showDialog,
                    primaryText = stringResource(id = android.R.string.ok),
                    primaryAction = {
                        repository.deleteFeed(removeItem.value!!)
                    }
                )
            }
        }
    }
}
