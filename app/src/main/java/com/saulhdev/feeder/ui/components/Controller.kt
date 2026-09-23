package com.saulhdev.feeder.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SwitchRow(
    modifier: Modifier = Modifier,
    text: String,
    withContainer: Boolean = false,
    index: Int = 0,
    groupSize: Int = 1,
    initSelected: () -> Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    val (selected, select) = remember(initSelected()) { mutableStateOf(initSelected()) }
    val interactionSource = remember { MutableInteractionSource() }
    val base = index.toFloat() / groupSize
    val rank = (index + 1f) / groupSize

    ListItem(
        modifier = modifier
            .fillMaxWidth(),
        shapes = ListItemDefaults.shapes(
            shape = RoundedCornerShape(
                topStart = if (base == 0f) MaterialTheme.shapes.large.topStart
                else MaterialTheme.shapes.extraSmall.topStart,
                topEnd = if (base == 0f) MaterialTheme.shapes.large.topEnd
                else MaterialTheme.shapes.extraSmall.topEnd,
                bottomStart = if (rank == 1f) MaterialTheme.shapes.large.bottomStart
                else MaterialTheme.shapes.extraSmall.bottomStart,
                bottomEnd = if (rank == 1f) MaterialTheme.shapes.large.bottomEnd
                else MaterialTheme.shapes.extraSmall.bottomEnd
            )
        ),
        colors = ListItemDefaults.colors(
            containerColor = if (withContainer) MaterialTheme.colorScheme.surfaceContainer
            else Color.Transparent,
        ),
        content = {
            Text(
                text = text,
                maxLines = 2,
            )
        },
        trailingContent = {
            Switch(
                checked = selected,
                interactionSource = interactionSource,
                onCheckedChange = {
                    select(it)
                    onCheckedChanged(it)
                }
            )
        },
        interactionSource = interactionSource,
        onClick = {
            select(!selected)
            onCheckedChanged(!selected)
        }
    )
}