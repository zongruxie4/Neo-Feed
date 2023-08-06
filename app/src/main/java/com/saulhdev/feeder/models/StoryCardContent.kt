package com.saulhdev.feeder.models

import android.os.Parcelable
import com.saulhdev.feeder.sdk.FeedCategory
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryCardContent(
    val title: String,
    val text: String,
    val background_url: String,
    val link: String,
    val source: FeedCategory
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }
}