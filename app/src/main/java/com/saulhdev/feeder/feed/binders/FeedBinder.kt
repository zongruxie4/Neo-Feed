package com.saulhdev.feeder.feed.binders

import android.view.View
import ua.itaysonlab.hfsdk.FeedItem

interface FeedBinder {
    fun bind(item: FeedItem, view: View)
}