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

import androidx.compose.runtime.Composable
import com.saulhdev.feeder.preference.FeedPreferences

val PreferenceBuilder =
    @Composable { pref: Any, onDialogPref: (Any) -> Unit, index: Int, size: Int ->
        when (pref) {
            is FeedPreferences.BooleanPref ->
                SwitchPreference(pref = pref, index = index, groupSize = size)

            is FeedPreferences.StringSetPref ->
                StringSetPreference(pref = pref, index = index, groupSize = size)

            is FeedPreferences.StringPref ->
                ActionPreference(pref = pref, index = index, groupSize = size)

            is FeedPreferences.FloatPref ->
                SeekBarPreference(pref = pref, index = index, groupSize = size)

            is FeedPreferences.StringSelectionPref ->
                StringSelectionPreference(
                    pref = pref,
                    index = index,
                    groupSize = size
                ) { onDialogPref(pref) }
        }
}