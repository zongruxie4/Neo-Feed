package com.saulhdev.feeder.preference

import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.utils.PreferenceHelper
import org.json.JSONObject

object HFPluginPreferences {
    private const val KEY = "HFFeedList"

    val feedList get() = PreferenceHelper.getSet(KEY)
    val parsedFeedList
        get() = PreferenceHelper.getSet(KEY).map {
            SavedFeedModel(JSONObject(it))
        }

    fun add(url: SavedFeedModel) {
        PreferenceHelper.setSet(KEY, feedList.apply {
            add(url.asJson().toString())
        })
    }

    fun remove(url: SavedFeedModel) {
        PreferenceHelper.setSet(KEY, feedList.apply {
            remove(url.asJson().toString())
        })
    }
}