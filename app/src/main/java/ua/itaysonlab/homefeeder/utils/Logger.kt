package ua.itaysonlab.homefeeder.utils

import android.util.Log
import com.saulhdev.feeder.BuildConfig

/**
 * This class wraps [android.util.Log].
 */
object Logger {
    fun log(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d("HF:$tag", msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }
}