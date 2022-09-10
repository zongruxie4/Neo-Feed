package ua.itaysonlab.hfsdk.content

import kotlinx.android.parcel.Parcelize
import ua.itaysonlab.hfsdk.FeedCategory

/**
 * [BaseContent] adapted for [ua.itaysonlab.hfsdk.FeedItemType.STORY_CARD]
 */

@Parcelize
data class StoryCardContent (
    val title: String,
    val text: String,
    val background_url: String, // network
    val link: String,
    val source: FeedCategory
): BaseContent()