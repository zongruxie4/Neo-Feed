package ua.itaysonlab.hfsdk

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.android.parcel.Parcelize
import ua.itaysonlab.hfsdk.content.BaseContent

@Parcelize
data class FeedCategory(
    val id: String,
    val title: String,
    @ColorInt val category_color: Int,
    val server_icon: String?
): Parcelable