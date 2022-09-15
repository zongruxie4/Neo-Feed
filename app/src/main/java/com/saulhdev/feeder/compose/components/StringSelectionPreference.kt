package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))
                )
            }
            if (pref.icon is Int) {
                Image(
                    painter = painterResource(id = pref.icon),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))
                )
            }
        },
        isEnabled = isEnabled,
        onClick = onClick
    )
}