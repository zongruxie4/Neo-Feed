package com.saulhdev.feeder.overlay

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.libraries.gsa.d.a.OverlayController
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.pages.OverlayPage
import com.saulhdev.feeder.utils.OverlayBridge


class ComposeOverlayView(val context: Context) :
    OverlayController(context, R.style.AppTheme, R.style.WindowTheme),
    OverlayBridge.OverlayBridgeCallback {

    private lateinit var rootView: View
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        getWindow().decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        rootView = View.inflate(
            ContextThemeWrapper(this, R.style.AppTheme),
            R.layout.overlay_layout,
            this.container
        )
        rootView.apply {
            ViewCompositionStrategy.DisposeOnLifecycleDestroyed(
                NFApplication.mainActivity!!.lifecycle
            )
        }
        rootView.findViewById<ComposeView>(R.id.overlay_view).setContent {
            OverlayPage()
        }

        NFApplication.bridge.setCallback(this)
    }

    override fun applyNewTheme(value: String) {
        TODO("Not yet implemented")
    }

    override fun applyNewCardBg(value: String) {
        TODO("Not yet implemented")
    }

    override fun applyCompactCard(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun applySysColors(value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun applyNewTransparency(value: Float) {
        TODO("Not yet implemented")
    }

    override fun onClientMessage(action: String) {
        TODO("Not yet implemented")
    }
}
