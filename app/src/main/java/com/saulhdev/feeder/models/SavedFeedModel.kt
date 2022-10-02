package com.saulhdev.feeder.models

import org.json.JSONObject

data class SavedFeedModel(
    val title: String,
    val description: String,
    val url: String,
    val feedImage: String = "",
    val isError: Boolean = false,
) {
    constructor(obj: JSONObject) : this(
        obj.getString("title"),
        obj.getString("description"),
        obj.getString("url"),
        obj.getString("feedImage"),
        obj.getBoolean("isError")
    )

    fun asJson() = JSONObject().apply {
        put("title", title)
        put("description", description)
        put("url", url)
        put("feedImage", feedImage)
        put("isError", isError)
    }
}