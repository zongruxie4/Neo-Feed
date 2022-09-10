package ua.itaysonlab.hfsdk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ua.itaysonlab.hfsdk.content.BaseContent

@Parcelize
data class FeedItem(
    // That "Application name" string
    val title: String,
    val type: FeedItemType,
    val content: BaseContent,
    val time: Long
): Parcelable