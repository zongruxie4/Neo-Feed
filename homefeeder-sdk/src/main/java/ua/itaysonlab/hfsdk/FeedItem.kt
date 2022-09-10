package ua.itaysonlab.hfsdk

import android.os.Parcel
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
): Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }
}