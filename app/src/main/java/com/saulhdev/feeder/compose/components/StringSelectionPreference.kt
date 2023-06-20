package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.saulhdev.feeder.preference.FeedPreferences

@Composable
fun StringSelectionPreference(
    modifier: Modifier = Modifier,
    pref: FeedPreferences.StringSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.entries[pref.onGetValue()],
        index = index,
        groupSize = groupSize,
        startWidget = {
            if (pref.icon is String) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(data = pref.icon)
                            .apply(block = fun ImageRequest.Builder.() {
                                crossfade(true)
                            }).build()
                    ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(36.dp)
                )
            }
            if (pref.icon is Int) {
                Image(
                    painter = painterResource(id = pref.icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(36.dp)
                )
            }
            if (pref.icon is ImageVector) {
                Image(
                    imageVector = pref.icon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .size(30.dp)
                )
            }
        },
        isEnabled = isEnabled,
        onClick = onClick
    )
}