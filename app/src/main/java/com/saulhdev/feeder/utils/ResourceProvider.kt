package com.saulhdev.feeder.utils

import android.content.Context
import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes id: Int, vararg args: Any): String
}

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(@StringRes id: Int, vararg args: Any) =
        context.getString(id, *args)
}