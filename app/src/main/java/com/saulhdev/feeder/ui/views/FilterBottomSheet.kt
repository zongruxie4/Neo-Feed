/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
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

package com.saulhdev.feeder.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.viewmodels.SourceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

@SuppressLint("ViewConstructor")
class FilterBottomSheet(
    context: Context,
    private val callback: () -> Unit
) : FrameLayout(context), View.OnClickListener {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val viewModel: SourceViewModel by inject(SourceViewModel::class.java)
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)
    private val sourcesChipGroup: ChipGroup
    private val tagsChipGroup: ChipGroup
    private val sortingChipGroup: ChipGroup
    private val filterOption: MaterialButtonToggleGroup

   init {
       View.inflate(context, R.layout.sort_filter_sheet, this)
       val container = findViewById<ViewGroup>(R.id.sort_filter_sheet)

       findViewById<View>(R.id.btn_apply)?.setOnClickListener(this)
       findViewById<View>(R.id.btn_reset)?.setOnClickListener(this)

       sourcesChipGroup = findViewById(R.id.all_sources_group)
       tagsChipGroup = findViewById(R.id.all_tags_group)
       sortingChipGroup = findViewById(R.id.cg_sort_options)
       filterOption = findViewById(R.id.toggle_sort_direction)

       getSources()
       getAllTags()
   }

    private fun getAllTags() {
        mainScope.launch {
            val selectedTags: ArrayList<String> =
                prefs.tagsFilter.getValue().toCollection(ArrayList())
            viewModel.allTags.collect { tags ->
                tagsChipGroup.removeAllViews()
                tags.forEach { tagName ->
                    val chip = createChip(tagName, selectedTags.contains(tagName)) {
                        if (it) {
                            selectedTags.add(tagName)
                        } else {
                            selectedTags.remove(tagName)
                        }
                    }
                    tagsChipGroup.addView(chip)
                }
            }
        }
    }

    private fun createChip(chipName: String, checked: Boolean, callback: (Boolean) -> Unit): Chip {
        val chip = Chip(context).apply {
            text = chipName
            isCheckable = true
            isChecked = checked
            checkedIcon = AppCompatResources.getDrawable(context, R.drawable.ic_check_24)
            checkedIconTint = getColorStateList()
            chipIcon = AppCompatResources.getDrawable(context, R.drawable.ic_circle_24dp)
            chipStrokeColor = getColorStateList()
            chipStrokeWidth = 1f
            setOnClickListener {
                chipIcon = if (isChecked) {
                    AppCompatResources.getDrawable(context, R.drawable.ic_check_24)
                } else {
                    AppCompatResources.getDrawable(context, R.drawable.ic_circle_24dp)
                }
            }
            callback(isChecked)
        }
        return chip
    }

    private fun getSources() {
        val selectedSources: ArrayList<String> =
            prefs.sourcesFilter.getValue().toCollection(ArrayList())
        mainScope.launch {
            viewModel.allEnabledFeeds.collect { sourceList ->
                val sources = sourceList.map { it.title }
                sourcesChipGroup.removeAllViews()
                sources.forEach { sourceName ->
                    val chip = createChip(sourceName, selectedSources.contains(sourceName)) {
                        if (it) {
                            selectedSources.add(sourceName)
                        } else {
                            selectedSources.remove(sourceName)
                        }
                    }
                    sourcesChipGroup.addView(chip)
                }
            }
        }
    }

    private fun getColorStateList(): ColorStateList? {
        val colorAttribute = com.google.android.material.R.attr.colorPrimary
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttribute, typedValue, true)
        return ContextCompat.getColorStateList(context, typedValue.resourceId)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_apply -> {
                val selectedSources: Set<String> = sourcesChipGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                val selectedTags: Set<String> = tagsChipGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                val sortingFilter = filterOption.children
                    .filterIsInstance<MaterialButton>()
                    .filter { it.isChecked }
                    .map { it.id }
                    .firstOrNull() ?: R.id.btn_sort_desc

                val sortingOption = when (sortingFilter) {
                    R.id.btn_sort_asc -> true
                    R.id.btn_sort_desc -> false
                    else -> true
                }

                val sorting = sortingChipGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString().lowercase() }
                    .firstOrNull() ?: context.getString(R.string.sorting_chronological)

                prefs.sourcesFilter.setValue(selectedSources)
                prefs.tagsFilter.setValue(selectedTags)
                prefs.sortingAsc.setValue(sortingOption)
                prefs.sortingFilter.setValue(sorting)
            }

            R.id.btn_reset -> {
                prefs.sourcesFilter.setValue(emptySet())
                prefs.tagsFilter.setValue(emptySet())
                prefs.sortingAsc.setValue(false)
                prefs.sortingFilter.setValue(context.getString(R.string.sorting_chronological))
            }

            else -> {
                Log.w("FilterBottomSheet", "Unknown button clicked: ${v.id}")
            }
        }
        callback()
    }

    companion object {
        fun show(
            context: Context,
            animate: Boolean
        ) {
            val sheet = BaseBottomSheet.inflate(context)
            sheet.show(FilterBottomSheet(context) {
                sheet.close(true)
            }, animate)
        }

    }
}