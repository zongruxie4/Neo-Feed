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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.db.Feed

@Composable
fun FeedItem(
    modifier: Modifier = Modifier,
    repository: Feed,
    onClickAction: (Feed) -> Unit = {},
    onRemoveAction: (Feed) -> Unit = {}
) {
    val (isEnabled, enable) = remember(repository.isEnabled) {
        mutableStateOf(repository.isEnabled)
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.surfaceColorAtElevation(32.dp)
        else MaterialTheme.colorScheme.background
    )
    Surface(
        modifier = modifier
            .clickable { onClickAction(repository) }
            .fillMaxWidth()
            .padding(8.dp),
        color = backgroundColor,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = repository.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = repository.url.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = repository.description,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = { onRemoveAction(repository) }
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }


        }

    }
}
