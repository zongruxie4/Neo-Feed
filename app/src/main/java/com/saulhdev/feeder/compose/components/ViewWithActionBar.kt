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

package com.saulhdev.feeder.compose.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWithActionBar(
    title: String,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    showBackButton: Boolean = true,
    actions: @Composable() (RowScope.() -> Unit) = {},
    onBackAction: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = if (showBackButton) {
                    {
                        val backDispatcher =
                            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                        IconButton(
                            onClick = {
                                onBackAction.invoke()
                                backDispatcher?.onBackPressed()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.go_back),
                            )
                        }
                    }
                } else null,
                actions = actions,
                backgroundColor = MaterialTheme.colorScheme.background,
                elevation = 0.dp
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content
    )
}