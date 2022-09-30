package com.saulhdev.feeder.models

import org.json.JSONObject

data class SavedFeedModel(
    val title: String,
    val description: String,
    val url: String,
    val feedImage: String = ""
) {
    constructor(obj: JSONObject) : this(
        obj.getString("name"),
        obj.getString("desc"),
        obj.getString("feed_url"),
        obj.getString("pic_url")
    )

    fun asJson() = JSONObject().apply {
        put("name", title)
        put("desc", description)
        put("feed_url", url)
        put("pic_url", feedImage)
    }
}