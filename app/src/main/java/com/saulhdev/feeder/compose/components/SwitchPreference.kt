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

package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.utils.addIf

@Composable
fun SwitchPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    startIcon: (@Composable () -> Unit)? = null,
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)
) {
    val itemIsChecked = remember { mutableStateOf(isChecked) }

    Row(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable(enabled = isEnabled) {
            itemIsChecked.value = !itemIsChecked.value
        }
    ) {
        startIcon?.let {
            startIcon()
            Spacer(modifier = Modifier.requiredWidth(16.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .addIf(!isEnabled) {
                    alpha(0.3f)
                }
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp
            )
            if (summary != "") {
                Text(
                    text = summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Switch(
            modifier = Modifier
                .height(24.dp),
            checked = itemIsChecked.value,
            onCheckedChange = {
                onCheckedChange(it)
            },
            enabled = isEnabled,
        )
    }
}