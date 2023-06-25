package com.saulhdev.feeder.overlay

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.libraries.gsa.d.a.OverlayController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.db.FeedRepository
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
import com.saulhdev.feeder.utils.setLightFlags
import com.saulhdev.feeder.vkpopup.DialogActionsVcByPopup
import com.saulhdev.feeder.vkpopup.PopupItem
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import ua.itaysonlab.hfsdk.FeedItem

class OverlayView(val context: Context) :
    OverlayController(context, R.style.AppTheme, R.style.WindowTheme),
    OverlayBridge.OverlayBridgeCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    var apiInstance = LauncherAPI()
    private lateinit var themeHolder: OverlayThemeHolder
    var sharedPrefs: SharedPreferences =
        context.getSharedPreferences("com.saulhdev.neofeed.prefs", Context.MODE_PRIVATE)

    private lateinit var rootView: View
    private lateinit var adapter: FeedAdapter

    private val list = mutableListOf<FeedItem>()
    val prefs = FeedPreferences(context)

    private fun setTheme(force: String?) {
        themeHolder.setTheme(
            when (force ?: prefs.overlayTheme.onGetValue()) {
                "auto_launcher" -> {
                    if (apiInstance.darkTheme) {
                        Theming.defaultDarkThemeColors
                    } else {
                        Theming.defaultLightThemeColors
                    }
                }

                "auto_system" -> Theming.getThemeBySystem(context)
                "dark" -> Theming.defaultDarkThemeColors
                else -> Theming.defaultLightThemeColors
            }
        )
    }

    override fun onOptionsUpdated(bundle: Bundle) {
        super.onOptionsUpdated(bundle)
        apiInstance = LauncherAPI(bundle)
        updateTheme()
    }

    private fun updateTheme(force: String? = null) {
        setTheme(force)
        updateStubUi()
        adapter.setTheme()
    }

    private fun updateStubUi() {
        val theme = if (themeHolder.currentTheme.get(Theming.Colors.OVERLAY_BG.ordinal)
                .isDark()
        ) Theming.defaultDarkThemeColors else Theming.defaultLightThemeColors
        rootView.findViewById<ImageView>(R.id.header_preferences).imageTintList =
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

        buttonReturnToTop.setOnClickListener {
            buttonReturnToTop.visibility = View.GONE
            recyclerView.smoothScrollToPosition(0)
        }

    }

    private fun initHeader() {
        rootView.findViewById<View>(R.id.header_preferences).setOnClickListener {
            callMenuPopup(it)
        }
    }

    private fun callMenuPopup(view: View) {
        val popup = DialogActionsVcByPopup(view)
        popup.a(createMenuList(), {
            it.first.backgroundTintList = ColorStateList.valueOf(
                themeHolder.currentTheme.get(
                    Theming.Colors.OVERLAY_BG.ordinal
                )
            )
            it.second.apply {
                setActionLabelTextColor(themeHolder.currentTheme.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
                setDividerColor(themeHolder.currentTheme.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
                setActionIconTint(themeHolder.currentTheme.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
            }
        }) {
            popup.dismiss()
            when (it.id) {
                "config" -> {
                    NFApplication.instance.startActivity(
                        Intent(NFApplication.instance, MainActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                }

                "reload" -> {
                    refreshNotifications()
                }

                "restart" -> {
                    NFApplication.instance.restart(false)
                }
            }
        }
    }

    private fun createMenuList(): List<PopupItem> {
        return listOf(
            PopupItem(R.drawable.ic_settings_outline_28, R.string.title_settings, 0, "config"),
            PopupItem(R.drawable.ic_replay_24, R.string.action_reload, 0, "reload"),
            PopupItem(R.drawable.ic_restart, R.string.action_restart, 1, "restart")
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
        NFApplication.bridge.setCallback(this)
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    private fun loadArticles() {
        val repository = FeedRepository(context)
        val articles = SyncRestClient(context)
        val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedSync")
        scope.launch {
            val feeds = repository.getAllFeeds()
            for (feed in feeds) {
                articles.getArticleList(feed)
            }
        }
    }

    private fun refreshNotifications() {
        list.clear()
        rootView.findViewById<RecyclerView>(R.id.recycler).recycledViewPool.clear()
        adapter.notifyDataSetChanged()
        loadArticles()

        PluginConnector.getFeedAsItLoads(0, { feed ->
            list.addAll(feed)
        }) {
            list.sortByDescending { it.time }
            adapter.replace(list)
            rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh).isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NFApplication.bridge.setCallback(null)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onScroll(f: Float) {
        super.onScroll(f)
        if (prefs.overlayTransparency.onGetValue() > 0.7f) {
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
            (prefs.overlayTransparency.onGetValue() * 255.0f).toInt() shl 24 or (bgColor and 0x00ffffff)
        getWindow().setBackgroundDrawable(ColorDrawable(color))
    }

    override fun onClientMessage(action: String) {
        if (prefs.debugging.onGetValue()) {
            Log.d("OverlayView", "New message by OverlayBridge: $action")
        }
    }

    override fun applyNewTheme(value: String) {
        updateTheme(value)
    }

    override fun applyNewTransparency(value: Float) {
        themeHolder.prefs.overlayTransparency.onSetValue(value)
    }

    override fun applyNewCardBg(value: String) {
        themeHolder.cardBgPref = value
        updateTheme()
    }

    override fun applyNewOverlayBg(value: String) {
        themeHolder.overlayBgPref = value
        updateTheme()
    }

    override fun applyCompactCard(value: Boolean) {
        adapter = FeedAdapter()
        adapter.setTheme()
        rootView.findViewById<RecyclerView>(R.id.recycler).adapter = adapter
        refreshNotifications()
    }

    override fun applySysColors(value: Boolean) {
        themeHolder.systemColors = value
        updateTheme()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }
}