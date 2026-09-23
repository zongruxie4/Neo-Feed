package com.saulhdev.feeder.ui.components.preferences

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.AppThemePref
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Swatches
import com.saulhdev.feeder.utils.extensions.themeSummary

@Composable
fun ThemePreference(
    modifier: Modifier = Modifier,
    pref: AppThemePref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val context = LocalContext.current
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = context.themeSummary(pref.getValue()),
        index = index,
        groupSize = groupSize,
        isEnabled = isEnabled,
        startWidget = {
            Icon(
                imageVector = Phosphor.Swatches,
                contentDescription = stringResource(R.string.theme),
            )
        },
        onClick = onClick,
    )
}
