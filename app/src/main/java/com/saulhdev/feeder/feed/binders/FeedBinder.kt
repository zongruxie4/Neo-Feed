package com.saulhdev.feeder.feed.binders

import android.util.SparseIntArray
import android.view.View
import com.saulhdev.feeder.sdk.FeedItem

interface FeedBinder {
    fun bind(theme: SparseIntArray?, item: FeedItem, view: View)
}