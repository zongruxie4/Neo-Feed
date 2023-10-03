/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.overlay

import android.content.Intent
import android.view.View
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.apps.gsa.nowoverlayservice.ConfigurationOverlayController
import com.google.android.libraries.gsa.d.a.OverlaysController

class DrawerOverlayService2 : LifecycleService(), SavedStateRegistryOwner {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private lateinit var overlaysController: OverlaysController

    private lateinit var contentView: View
    override fun onCreate() {
        super.onCreate()
        // init your SavedStateRegistryController
        savedStateRegistryController.performAttach() // you can ignore this line, becase performRestore method will auto call performAttach() first.
        savedStateRegistryController.performRestore(null)

        // configure your ComposeView
        contentView = ComposeView(this).apply {
            setViewTreeSavedStateRegistryOwner(this@DrawerOverlayService2)
            setContent {
                Text(text = "Hello World")
            }
        }
        this.overlaysController = ConfigurationOverlayController(this)
        contentView.setViewTreeLifecycleOwner(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
        // add your contentView to windowManager
    }

    override fun onDestroy() {
        super.onDestroy()
        this.overlaysController.onDestroy()
        // remove your view from your windowManager
    }

    // override savedStateRegistry property from SavedStateRegistryOwner interface.
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}