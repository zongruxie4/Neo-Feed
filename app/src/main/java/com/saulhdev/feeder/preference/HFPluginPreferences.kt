package com.saulhdev.feeder.preference

import com.saulhdev.feeder.models.SavedFeedModel
import org.json.JSONObject
import ua.itaysonlab.hfrss.utils.PreferenceHelper

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