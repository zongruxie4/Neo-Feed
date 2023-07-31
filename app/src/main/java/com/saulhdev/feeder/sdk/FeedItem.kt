package com.saulhdev.feeder.sdk

import android.os.Parcelable
import com.saulhdev.feeder.models.StoryCardContent
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedItem(
    val id: Long,
    val title: String,
    val type: FeedItemType,
    val content: StoryCardContent,
    val bookmarked: Boolean,
    val time: Long
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }
}