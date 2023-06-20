package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.preference.FeedPreferences

@Composable
fun SeekBarPreference(
    modifier: Modifier = Modifier,
    pref: FeedPreferences.FloatPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onValueChange: ((Float) -> Unit) = {},
) {
    var currentValue by remember(pref) { mutableStateOf(pref.onGetValue()) }

    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        startWidget = {
            if (pref.icon is ImageVector) {
                Image(
                    imageVector = pref.icon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        bottomWidget = {
            Row {
                /*Text(
                    text = pref.specialOutputs(currentValue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.widthIn(min = 52.dp)
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))*/
                Slider(
                    modifier = Modifier
                        .requiredHeight(24.dp)
                        .weight(1f),
                    value = currentValue,
                    valueRange = pref.minValue..pref.maxValue,
                    onValueChange = { currentValue = it },
                    steps = pref.steps,
                    onValueChangeFinished = {
                        pref.onSetValue(currentValue)
                        onValueChange(currentValue)
                    },
                    enabled = isEnabled
                )
            }
        }
    )
}