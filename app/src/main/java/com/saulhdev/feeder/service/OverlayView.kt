package com.saulhdev.feeder.service

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.libraries.gsa.d.a.OverlayController
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.NeoApp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.entity.MenuItem
import com.saulhdev.feeder.extensions.isDark
import com.saulhdev.feeder.extensions.setCustomTheme
import com.saulhdev.feeder.manager.sync.SyncRestClient
import com.saulhdev.feeder.ui.feed.FeedAdapter
import com.saulhdev.feeder.ui.navigation.Routes
import com.saulhdev.feeder.ui.theme.CardTheme
import com.saulhdev.feeder.ui.theme.OverlayThemeHolder
import com.saulhdev.feeder.ui.views.AbstractFloatingView
import com.saulhdev.feeder.ui.views.DialogMenu
import com.saulhdev.feeder.ui.views.FilterBottomSheet
import com.saulhdev.feeder.utils.LinearLayoutManagerWrapper
import com.saulhdev.feeder.viewmodels.ArticlesViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

class OverlayView(val context: Context): OverlayController(context, R.style.AppTheme, R.style.WindowTheme),
    KoinComponent,OverlayBridge.OverlayBridgeCallback {
    private lateinit var themeHolder: OverlayThemeHolder
    private val syncScope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val viewModel: ArticlesViewModel by inject(ArticlesViewModel::class.java)
    private val articles: SyncRestClient by inject(SyncRestClient::class.java)
    val prefs: FeedPreferences by inject()

    var bookmarkVisible = false

    private lateinit var rootView: View
    private lateinit var mainContainer: ViewGroup
    private lateinit var adapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = View.inflate(
            ContextThemeWrapper(this, R.style.AppTheme),
            R.layout.overlay_layout,
            this.container
        )
        mainContainer = rootView.findViewById(R.id.overlay_root)
        AbstractFloatingView.container = container
        AbstractFloatingView.closeAllOpenViews(context)

        themeHolder = OverlayThemeHolder(this)

        initRecyclerView()
        initHeader()
        refreshNotifications()

        syncScope.launch {
            viewModel.articlesList.collect {
                mainScope.launch {
                    adapter.replace(it)
                }
            }
        }
        syncScope.launch {
            viewModel.isSyncing
                .collect {
                    mainScope.launch {
                        rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh).isRefreshing =
                            it
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
        NeoApp.bridge.setCallback(this)
    }

    override fun closePanelIfNeeded(flags: Int) {
        super.closePanelIfNeeded(flags)
        AbstractFloatingView.closeAllOpenViews(context)
    }

    private fun updateTheme(force: String? = null) {
        setTheme(force)
        updateStubUi()
        adapter.setTheme(themeHolder.currentTheme)
    }

    private fun setTheme(force: String?) {
        themeHolder.setTheme(
            when (force ?: prefs.overlayTheme.getValue()) {
                "auto_system_black" -> CardTheme.getThemeBySystem(context, true)
                "auto_system" -> CardTheme.getThemeBySystem(context, false)
                "dark" -> CardTheme.defaultDarkThemeColors
                "black" -> CardTheme.defaultBlackThemeColors
                else -> CardTheme.defaultLightThemeColors
            }
        )
        setCustomTheme()
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    private fun updateStubUi() {
        val theme = if (themeHolder.currentTheme.get(CardTheme.Colors.OVERLAY_BG.ordinal)
                .isDark()
        ) CardTheme.defaultDarkThemeColors else CardTheme.defaultLightThemeColors
        rootView.findViewById<MaterialButton>(R.id.header_settings).iconTint =
            ColorStateList.valueOf(
                theme.get(
                    CardTheme.Colors.TEXT_COLOR_PRIMARY.ordinal
                )
            )

        rootView.findViewById<MaterialButton>(R.id.header_filter).iconTint =
            ColorStateList.valueOf(
                theme.get(
                    CardTheme.Colors.TEXT_COLOR_PRIMARY.ordinal
                )
            )

        rootView.findViewById<TextView>(R.id.header_title)
            .setTextColor(theme.get(CardTheme.Colors.TEXT_COLOR_PRIMARY.ordinal))
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

    private fun updateToggleColor(button: MaterialButton, isChecked: Boolean) {
        val context = button.context
        val checkedColor = ColorUtils.setAlphaComponent(
            ContextCompat.getColor(context, R.color.toggle_checked),
            64
        )
        val uncheckedColor = Color.TRANSPARENT
        button.backgroundTintList =
            ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)

    }

    private fun initHeader() {
        val bookmarkButton = rootView.findViewById<MaterialButton>(R.id.header_bookmark)

        updateToggleColor(bookmarkButton, bookmarkVisible)
        bookmarkButton.setOnClickListener {
            mainScope.launch {
                if (bookmarkVisible) {
                    bookmarkVisible = false
                    bookmarkButton.isChecked = bookmarkVisible
                    updateToggleColor(bookmarkButton, bookmarkVisible)
                    viewModel.articlesList.collect {
                        adapter.replace(it)
                        adapter.notifyDataSetChanged()

                    }
                } else {
                    bookmarkVisible = true
                    bookmarkButton.isChecked = bookmarkVisible
                    updateToggleColor(bookmarkButton, bookmarkVisible)
                    viewModel.bookmarkedArticlesList.collect {
                        adapter.replace(it)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        rootView.findViewById<MaterialButton>(R.id.header_filter).apply {
            setOnClickListener {
                FilterBottomSheet.show(context, true){

                }
            }
        }


        rootView.findViewById<MaterialButton>(R.id.header_settings).apply {
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
                "config" -> {
                    mainScope.launch {
                        view.context.startActivity(
                            MainActivity.navigateIntent(
                                view.context,
                                "${Routes.MAIN}/1",
                            )
                        )
                    }
                }

                "reload" -> {
                    rootView.findViewById<RecyclerView>(R.id.recycler).recycledViewPool.clear()
                    refreshNotifications()
                }

                "restart" -> {
                    val application: NeoApp by inject(NeoApp::class.java)
                    application.restart(false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NeoApp.bridge.setCallback(null)
    }

    override fun onScroll(f: Float) {
        super.onScroll(f)

        val bgColor = themeHolder.currentTheme.get(CardTheme.Colors.OVERLAY_BG.ordinal)
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

    override fun applyCompactCard(value: Boolean) {
        adapter = FeedAdapter()
        adapter.setTheme(themeHolder.currentTheme)
        rootView.findViewById<RecyclerView>(R.id.recycler).adapter = adapter
        refreshNotifications()
    }

    private fun refreshNotifications() {
        syncScope.launch {
            articles.syncAllFeeds()
        }
    }

    private fun createMenuList(): List<MenuItem> {
        return listOf(
            MenuItem(R.drawable.ic_arrow_clockwise, R.string.action_reload, 0, "reload"),
            MenuItem(R.drawable.ic_gear, R.string.title_settings, 2, "config"),
            MenuItem(R.drawable.ic_power, R.string.action_restart, 2, "restart")
        )
    }
    companion object{

    }
}