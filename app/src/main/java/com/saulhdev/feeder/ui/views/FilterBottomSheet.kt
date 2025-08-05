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
import com.saulhdev.feeder.R

@SuppressLint("ViewConstructor")
class FilterBottomSheet(
    context: Context,
    private val callback: (Boolean) -> Unit
) : FrameLayout(context), View.OnClickListener {
   init {
       View.inflate(context, R.layout.sort_filter_sheet, this)
       val container = findViewById<ViewGroup>(R.id.sort_filter_sheet)

       findViewById<View>(R.id.btn_apply)?.setOnClickListener(this)
       findViewById<View>(R.id.btn_reset)?.setOnClickListener(this)

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