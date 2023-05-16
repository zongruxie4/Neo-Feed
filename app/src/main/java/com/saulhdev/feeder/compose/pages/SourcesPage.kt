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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.FeedItem
import com.saulhdev.feeder.compose.components.OverflowMenu
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.navigation.LocalNavController
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.models.exportOpml
import com.saulhdev.feeder.models.importOpml
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.viewmodel.SourcesViewModel
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import java.time.LocalDateTime

@Composable
fun SourcesPage(viewModel: SourcesViewModel) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val repository = FeedRepository(context)
    val localTime = LocalDateTime.now().toString().replace(":", "_").substring(0, 19)

    val di = LocalDI.current
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

    ViewWithActionBar(
        title = stringResource(id = R.string.title_sources),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("/${Routes.ADD_FEED}/")
                },
                modifier = Modifier.padding(16.dp),
                backgroundColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

        },
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.CloudDownload,
                            contentDescription = null,
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
                            Icons.Outlined.CloudUpload,
                            contentDescription = null,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
                )
        ) {
            val showDialog = remember { mutableStateOf(false) }
            val list: State<List<Feed>> = viewModel.feedSources.collectAsState()
            val removeItem: MutableState<Feed?> =
                remember { mutableStateOf(list.value.firstOrNull()) }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(list.value) { item ->
                    FeedItem(
                        repository = item,
                        onClickAction = {
                            navController.navigate(
                                "/edit_feed/${item.id}/"
                            )
                        },
                        onRemoveAction = {
                            showDialog.value = true
                            removeItem.value = item
                        }
                    )
                    if (showDialog.value) {
                        Dialog(
                            onDismissRequest = { showDialog.value = false },
                            DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.remove_title),
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                    fontFamily = FontFamily.Default,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = stringResource(
                                                id = R.string.remove_desc,
                                                removeItem.value!!.title
                                            ),
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontFamily = FontFamily.Default
                                            )
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            TextButton(
                                                shape = RoundedCornerShape(16.dp),
                                                onClick = {
                                                    showDialog.value = false
                                                }
                                            ) {
                                                Text(
                                                    text = stringResource(id = android.R.string.cancel),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(
                                                        vertical = 5.dp,
                                                        horizontal = 8.dp
                                                    )
                                                )
                                            }

                                            Spacer(Modifier.weight(1f))

                                            TextButton(
                                                shape = RoundedCornerShape(16.dp),
                                                onClick = {
                                                    repository.deleteFeed(removeItem.value!!)
                                                    showDialog.value = false
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary.copy(
                                                        0.65f
                                                    ),
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Text(
                                                    text = stringResource(id = android.R.string.ok),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(
                                                        top = 5.dp,
                                                        bottom = 5.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
