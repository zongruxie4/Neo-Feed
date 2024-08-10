package com.saulhdev.feeder.utils

class OverlayBridge {
    private var callback: OverlayBridgeCallback? = null

    fun setCallback(callback: OverlayBridgeCallback?) {
        this.callback = callback
    }

    interface OverlayBridgeCallback {
        fun applyNewTheme(value: String)
        //fun applyNewCardBg(value: String)
        fun applyCompactCard(value: Boolean)
        fun applySysColors(value: Boolean)
        fun applyNewTransparency(value: Float)
        fun onClientMessage(action: String)
    }
}