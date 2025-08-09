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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.adapter.SelectableAdapter
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.utils.LinearLayoutManagerWrapper
import com.saulhdev.feeder.viewmodels.SourceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

@SuppressLint("ViewConstructor")
class FilterBottomSheet(
    context: Context,
    private val callback: (Boolean) -> Unit
) : FrameLayout(context), View.OnClickListener {
    private var sourcesAdapter: SelectableAdapter
    private var tagsAdapter: SelectableAdapter
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val viewModel: SourceViewModel by inject(SourceViewModel::class.java)
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)

   init {
       View.inflate(context, R.layout.sort_filter_sheet, this)
       val container = findViewById<ViewGroup>(R.id.sort_filter_sheet)

       findViewById<View>(R.id.btn_apply)?.setOnClickListener(this)
       findViewById<View>(R.id.btn_reset)?.setOnClickListener(this)

       sourcesAdapter = SelectableAdapter(context)
       tagsAdapter = SelectableAdapter(context)

       findViewById<RecyclerView>(R.id.all_sources).apply {
           layoutManager = LinearLayoutManagerWrapper(context, LinearLayoutManager.VERTICAL, false)
           adapter = this@FilterBottomSheet.sourcesAdapter
       }
       getSources()

       findViewById<RecyclerView>(R.id.all_tags).apply {
           layoutManager = LinearLayoutManagerWrapper(context, LinearLayoutManager.VERTICAL, false)
           adapter = this@FilterBottomSheet.tagsAdapter
       }
       getAllTags()

   }

    private fun getAllTags() {
        mainScope.launch {
            val selectedTags: ArrayList<String> =
                prefs.tagsFilter.getValue().toCollection(ArrayList())
            viewModel.allTags.collect {
                tagsAdapter.replace(it, selectedTags)
            }
        }
    }

    private fun getSources() {
        val selectedSources: ArrayList<String> =
            prefs.sourcesFilter.getValue().toCollection(ArrayList())
        mainScope.launch {
            viewModel.allFeeds.collect { source ->
                val sources = source.map { it.title }
                sourcesAdapter.replace(sources, selectedSources)
            }
        }

    }

    override fun onClick(v: View) {
        Log.d("FilterBottomSheet", "onClick: ${v.id}")
        when (v.id) {
            R.id.btn_apply -> callback(true)
            R.id.btn_reset -> callback(false)
        }
    }

    companion object {
        fun show(
            context: Context,
            animate: Boolean,
            callback: () -> Unit
        ) {
            val sheet = BaseBottomSheet.inflate(context)
            sheet.show(FilterBottomSheet(context) {
                Log.d("FilterBottomSheet", "onClick: $it")

                if (it) {
                    callback()
                }
                sheet.close(true)
            }, animate)
        }

    }
}