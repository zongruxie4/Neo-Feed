package ua.itaysonlab.hfsdk.content

import kotlinx.android.parcel.Parcelize
import ua.itaysonlab.hfsdk.actions.CardAction

/**
 * [BaseContent] adapted for [ua.itaysonlab.hfsdk.FeedItemType.TEXT_CARD] and [ua.itaysonlab.hfsdk.FeedItemType.TEXT_CARD_ACTIONS]
 */

@Parcelize
data class TextCardContent (
    val title: String,
    val subtitle: String? = null,
    val text: String,
    val actions: List<CardAction>? = null
): BaseContent()