/*
 * This file is part of Neo Feed
 * Copyright (c) 2024   NeoApplications Team
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

package com.saulhdev.feeder.views

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.ActionStyle
import com.saulhdev.feeder.data.DividerStyle
import com.saulhdev.feeder.data.MenuItem
import kotlin.math.ceil

@UiThread
open class MenuListView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RecyclerView(context, attrs, defStyleAttr) {
    private val dividerStyle: DividerStyle
    private val actionStyle: ActionStyle
    private var menuDividerItem: MenuDividerItem
    private var actionAdapter: MenuAdapter

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.MenuListView, defStyleAttr, 0)
        val dividerHeight = attr.getDimensionPixelSize(R.styleable.MenuListView_dividerHeight, a(1))
        val dividerSize = attr.getDimensionPixelSize(R.styleable.MenuListView_dividerSize, a(1))
        val dividerColor = attr.getColor(R.styleable.MenuListView_dividerColor, -16777216)
        this.dividerStyle = DividerStyle(dividerHeight, dividerSize, dividerColor)

        val optionBackground = attr.getDrawable(R.styleable.MenuListView_optionBackground)
        val paddingStart =
            attr.getDimensionPixelSize(R.styleable.MenuListView_optionPaddingStart, 0)
        val paddingEnd = attr.getDimensionPixelSize(R.styleable.MenuListView_optionPaddingEnd, 0)
        val iconSpace = attr.getDimensionPixelSize(R.styleable.MenuListView_optionIconLabelSpace, 0)
        val iconTint =
            Integer.valueOf(attr.getColor(R.styleable.MenuListView_optionIconTint, -16777216))
        val textSize = attr.getDimensionPixelSize(
            R.styleable.MenuListView_optionLabelTextSize,
            (((getDisplayMetrics().scaledDensity * 16.0f) + 0.5f).toInt())
        )
        val textColor = attr.getColor(R.styleable.MenuListView_optionLabelTextColor, -16777216)
        this.actionStyle = ActionStyle(
            optionBackground,
            paddingStart,
            paddingEnd,
            iconSpace,
            iconTint,
            textSize,
            textColor
        )
        attr.recycle()

        this.menuDividerItem = MenuDividerItem(this.dividerStyle)
        this.actionAdapter = MenuAdapter(context, this.actionStyle)
        this.layoutManager = LinearLayoutManager(context, VERTICAL, false)
        this.addItemDecoration(this.menuDividerItem)
        this.adapter = this.actionAdapter
    }

    fun setDividerHeight(@Px i: Int) {
        this.dividerStyle.dividerHeight = i
        a()
    }

    fun setDividerSize(@Px i: Int) {
        this.dividerStyle.dividerSize = i
        a()
    }

    fun setDividerColor(@ColorInt i: Int) {
        this.dividerStyle.dividerColor = i
        a()
    }

    fun setActionBackground(@DrawableRes i: Int) {
        if (i != 0) {
            setActionBackground(ContextCompat.getDrawable(context, i))
        } else {
            setActionBackground(null)
        }
    }

    fun setActionBackground(drawable: Drawable?) {
        this.actionStyle.optionBackground = drawable
        b()
    }

    fun setActionPaddingStart(@Px i: Int) {
        this.actionStyle.paddingStart = i
        b()
    }

    fun setActionIconTint(@ColorInt i: Int) {
        this.actionStyle.iconTint = i
        b()
    }

    fun setActionPaddingEnd(@Px i: Int) {
        this.actionStyle.paddingEnd = i
        b()
    }

    fun setActionIconLabelSpace(@Px i: Int) {
        this.actionStyle.iconSpace = i
        b()
    }

    fun setActionLabelTextSize(@Px i: Int) {
        this.actionStyle.textSize = i
        b()
    }

    fun setActionLabelTextColor(@ColorInt i: Int) {
        this.actionStyle.textColor = i
        b()
    }

    fun setActionClickListener(cVar: (MenuItem) -> Unit) {
        this.actionAdapter.setOnClickListener(cVar)
    }

    fun setActions(list: List<MenuItem>) {
        this.actionAdapter.setAdapterItems(list)
        if (list.isNotEmpty()) layoutManager?.scrollToPosition(0)
    }

    private fun a() {
        this.menuDividerItem = MenuDividerItem(this.dividerStyle)
        adapter?.notifyDataSetChanged()
    }

    private fun b() {
        actionAdapter = MenuAdapter(context, this.actionStyle)
        adapter = this.actionAdapter
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }

    private fun a(i: Int): Int {
        return ceil((getDisplayMetrics().density * 1.0f)).toInt()
    }
}