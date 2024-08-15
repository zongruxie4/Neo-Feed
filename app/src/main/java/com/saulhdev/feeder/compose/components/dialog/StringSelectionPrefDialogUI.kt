/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saulhdev.feeder.compose.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.preference.StringSelectionPref
import kotlinx.coroutines.launch

@Composable
fun StringSelectionPrefDialogUI(
    pref: StringSelectionPref,
    openDialogCustom: MutableState<Boolean>
) {
    var selected by remember { mutableStateOf(pref.getValue()) }
    val entryPairs = pref.entries.toList()

    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f, false)
            ) {
                items(items = entryPairs, key = { it.first }) {
                    val isSelected = rememberSaveable(selected) {
                        mutableStateOf(selected == it.first)
                    }
                    SingleSelectionListItem(
                        text = it.second,
                        isSelected = isSelected.value
                    ) {
                        selected = it.first
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = {
                        scope.launch {
                            openDialogCustom.value = false
                            pref.setValue(selected)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SingleSelectionListItem(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = isEnabled),
        leadingContent = {
            RadioButton(
                selected = isSelected,
                enabled = isEnabled,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        headlineContent = {
            Text(text = text)
        }
    )
}