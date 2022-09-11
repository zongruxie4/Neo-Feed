package com.saulhdev.feeder.models

import org.json.JSONObject

data class SavedFeedModel(
    val name: String,
    val description: String,
    val feedUrl: String,
    val feedImage: String
) {
    constructor(obj: JSONObject) : this(
        obj.getString("name"),
        obj.getString("desc"),
        obj.getString("feed_url"),
        obj.getString("pic_url")
    )

    fun asJson() = JSONObject().apply {
        put("name", name)
        put("desc", description)
        put("feed_url", feedUrl)
        put("pic_url", feedImage)
    }
}