package com.saulhdev.feeder.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        isEnabled = isEnabled,
        onClick = onClick
    )
}