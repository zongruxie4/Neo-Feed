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

package com.saulhdev.feeder.overlay

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.libraries.gsa.d.a.OverlayController
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.data.MenuItem
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.feed.FeedAdapter
import com.saulhdev.feeder.launcherapi.LauncherAPI
import com.saulhdev.feeder.launcherapi.OverlayThemeHolder
import com.saulhdev.feeder.plugin.PluginConnector
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sync.SyncRestClient
import com.saulhdev.feeder.theme.Theming
import com.saulhdev.feeder.utils.LinearLayoutManagerWrapper
import com.saulhdev.feeder.utils.OverlayBridge
import com.saulhdev.feeder.utils.clearLightFlags
import com.saulhdev.feeder.utils.isDark
import com.saulhdev.feeder.utils.setCustomTheme
import com.saulhdev.feeder.utils.setLightFlags
import com.saulhdev.feeder.views.DialogMenu
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.java.KoinJavaComponent.inject


class OverlayView(val context: Context) :
    OverlayController(context, R.style.AppTheme, R.style.WindowTheme),
    OverlayBridge.OverlayBridgeCallback {
    private var apiInstance = LauncherAPI()
    private lateinit var themeHolder: OverlayThemeHolder
    private val syncScope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private lateinit var rootView: View
    private lateinit var adapter: FeedAdapter
    private val repository: ArticleRepository by inject(ArticleRepository::class.java)
    private val articles: SyncRestClient by inject(SyncRestClient::class.java)

    val prefs = FeedPreferences.getInstance(context)


    private fun setTheme(force: String?) {
        themeHolder.setTheme(
            when (force ?: prefs.overlayTheme.getValue()) {
                "auto_system_black" -> Theming.getThemeBySystem(context, true)
                "auto_system"       -> Theming.getThemeBySystem(context, false)
                "dark"              -> Theming.defaultDarkThemeColors
                "black"             -> Theming.defaultBlackThemeColors
                else                -> Theming.defaultLightThemeColors
            }
        )
        setCustomTheme()
    }

    override fun onOptionsUpdated(bundle: Bundle) {
        super.onOptionsUpdated(bundle)
        apiInstance = LauncherAPI(bundle)
        updateTheme()
    }

    private fun updateTheme(force: String? = null) {
        setTheme(force)
        updateStubUi()
        adapter.setTheme(themeHolder.currentTheme)
    }

    private fun updateStubUi() {
        val theme = if (themeHolder.currentTheme.get(Theming.Colors.OVERLAY_BG.ordinal)
                .isDark()
        ) Theming.defaultDarkThemeColors else Theming.defaultLightThemeColors
        rootView.findViewById<MaterialButton>(R.id.header_preferences).iconTint =
            ColorStateList.valueOf(
                theme.get(
                    Theming.Colors.TEXT_COLOR_PRIMARY.ordinal
                )
            )
        rootView.findViewById<TextView>(R.id.header_title)
            .setTextColor(theme.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
    }


    private fun initRecyclerView() {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler)
        val buttonReturnToTop =
            rootView.findViewById<FloatingActionButton>(R.id.button_return_to_top).apply {
                visibility = View.GONE
                setOnClickListener {
                    visibility = View.GONE
                    recyclerView.smoothScrollToPosition(0)

                }
            }

        rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh).setOnRefreshListener {
            rootView.findViewById<RecyclerView>(R.id.recycler).recycledViewPool.clear()
            refreshNotifications()
        }

        adapter = FeedAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManagerWrapper(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@OverlayView.adapter
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if ((recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition() < 5
                ) {
                    buttonReturnToTop.visibility = View.GONE
                } else if ((recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition() > 5
                ) {
                    buttonReturnToTop.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun initHeader() {

        rootView.findViewById<MaterialButton>(R.id.header_preferences).apply {
            setOnClickListener {
                openMenu(it)
            }
        }
    }

    private fun openMenu(view: View) {
        val popup = DialogMenu(view)
        popup.show(createMenuList()) {
            popup.dismiss()
            when (it.id) {
                "config"  -> {
                    mainScope.launch {
                        view.context.startActivity(
                            MainActivity.navigateIntent(
                                view.context,
                                "${Routes.MAIN}/1",
                            )
                        )
                    }
                }
                /*
                "import" -> {
                    scope.launch {
                        val intent = MainActivity.createIntent(context,"","import")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        val activity = getActivity(context.applicationContext)
                        if(activity!=null)
                            MainActivity.startActivityForResult(activity, intent)

                    }
                }

                "export" ->  {
                    scope.launch {
                        val intent = MainActivity.createIntent(context,"","export")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        val activity = getActivity(context.applicationContext)
                        if(activity!=null)
                            MainActivity.startActivityForResult(activity, intent)
                    }
                }
                */

                "reload"  -> {
                    refreshNotifications()
                }

                "restart" -> {
                    val application: NFApplication by inject(NFApplication::class.java)
                    application.restart(false)
                }
            }
        }
    }

    private fun getActivity(context: Context): Activity? {
        if (context is Activity) return context
        if (context is ContextWrapper) return getActivity(context.baseContext)
        return null
    }

    private fun createMenuList(): List<MenuItem> {
        return listOf(
            MenuItem(R.drawable.ic_arrow_clockwise, R.string.action_reload, 0, "reload"),
            //MenuItem(R.drawable.ic_cloud_arrow_down, R.string.sources_import_opml, 1, "import"),
            //MenuItem(R.drawable.ic_cloud_arrow_up, R.string.sources_export_opml, 1, "export"),
            MenuItem(R.drawable.ic_gear, R.string.title_settings, 2, "config"),
            MenuItem(R.drawable.ic_power, R.string.action_restart, 2, "restart")
        )
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        getWindow().decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        rootView = View.inflate(
            ContextThemeWrapper(this, R.style.AppTheme),
            R.layout.overlay_layout,
            this.container
        )

        themeHolder = OverlayThemeHolder(context, this)

        initRecyclerView()
        initHeader()
        refreshNotifications()
        syncScope.launch {
            repository.getFeedArticles()
                .mapLatest { articles ->
                    (if (prefs.removeDuplicates.getValue()) articles.distinctBy { it.content.link }
                    else articles)
                        .sortedByDescending { it.time }
                }.collect {
                    mainScope.launch {
                        adapter.replace(it)
                    }
                }
        }
        syncScope.launch {
            prefs.overlayTheme.get().collect {
                mainScope.launch {
                    applyNewTheme(it)
                }
            }
        }
        NFApplication.bridge.setCallback(this)
    }

    private fun refreshNotifications() {
        syncScope.launch {
            val feeds = repository.getAllFeeds()
            for (feed in feeds) {
                articles.getArticleList(feed)
            }
        }
        PluginConnector.getFeedAsItLoads(0, { }) {
            rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh).isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NFApplication.bridge.setCallback(null)
    }

    override fun onScroll(f: Float) {
        super.onScroll(f)
        if (prefs.overlayTransparency.getValue() > 0.7f) {
            if (themeHolder.shouldUseSN && !themeHolder.isSNApplied) {
                themeHolder.isSNApplied = true
                window.decorView.setLightFlags()
            }
        } else {
            if (themeHolder.shouldUseSN && themeHolder.isSNApplied) {
                themeHolder.isSNApplied = false
                window.decorView.clearLightFlags()
            }
        }

        val bgColor = themeHolder.currentTheme.get(Theming.Colors.OVERLAY_BG.ordinal)
        val color =
            (prefs.overlayTransparency.getValue() * 255.0f).toInt() shl 24 or (bgColor and 0x00ffffff)
        getWindow().setBackgroundDrawable(ColorDrawable(color))
    }

    override fun onClientMessage(action: String) {
        if (prefs.debugging.getValue()) {
            Log.d("OverlayView", "New message by OverlayBridge: $action")
        }
    }

    override fun applyNewTheme(value: String) {
        updateTheme(value)
    }

    override fun applyNewTransparency(value: Float) {
        themeHolder.prefs.overlayTransparency.setValue(value)
    }

    /*override fun applyNewCardBg(value: String) {
        themeHolder.cardBgPref = value
        updateTheme()
    }*/

    override fun applyCompactCard(value: Boolean) {
        adapter = FeedAdapter()
        adapter.setTheme(themeHolder.currentTheme)
        rootView.findViewById<RecyclerView>(R.id.recycler).adapter = adapter
        refreshNotifications()
    }

    override fun applySysColors(value: Boolean) {
        themeHolder.systemColors = value
        updateTheme()
    }
}