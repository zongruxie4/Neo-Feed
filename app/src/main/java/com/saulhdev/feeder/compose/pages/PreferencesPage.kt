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

package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.BaseDialog
import com.saulhdev.feeder.compose.components.PreferenceGroup
import com.saulhdev.feeder.compose.components.StringSelectionPrefDialogUI
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.preference.FeedPreferences

@Composable
fun PreferencesPage() {
    val title = stringResource(id = R.string.title_settings)
    ViewWithActionBar(
        title = title,
    ) { paddingValues ->
        val prefs = FeedPreferences(LocalContext.current)
        val themePrefs = listOf(
            prefs.overlayTheme,
            prefs.overlayTransparency,
            prefs.systemColors,
            prefs.overlayBackground,
            prefs.cardBackground
        )

        val debugPrefs = listOf(
            prefs.about
        )
        val sourcePrefs = listOf(
            prefs.sources,
            prefs.bookmarks,
            prefs.openInBrowser,
            prefs.syncOnlyOnWifi,
            prefs.syncFrequency,
            prefs.itemsPerFeed,
            prefs.offlineReader
        )
        val openDialog = remember { mutableStateOf(false) }
        var dialogPref by remember { mutableStateOf<Any?>(null) }
        val onPrefDialog = { pref: Any ->
            dialogPref = pref
            openDialog.value = true
        }

        LazyColumn(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(
                    stringResource(id = R.string.title_sources),
                    prefs = sourcePrefs,
                    onPrefDialog = onPrefDialog
                )
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_overlay),
                    prefs = themePrefs,
                    onPrefDialog = onPrefDialog
                )
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_debug),
                    prefs = debugPrefs,
                    onPrefDialog = onPrefDialog
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref) {
                    is FeedPreferences.StringSelectionPref -> StringSelectionPrefDialogUI(
                        pref = dialogPref as FeedPreferences.StringSelectionPref,
                        openDialogCustom = openDialog
                    )
                }
            }
        }
    }
}