package ua.itaysonlab.hfsdk.content

import android.os.Parcel
import kotlinx.android.parcel.Parcelize
import ua.itaysonlab.hfsdk.FeedCategory

@Parcelize
data class StoryCardContent(
    val title: String,
    val text: String,
    val background_url: String,
    val link: String,
    val source: FeedCategory
) : BaseContent() {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }
}