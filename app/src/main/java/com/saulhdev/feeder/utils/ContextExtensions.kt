package com.saulhdev.feeder.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

interface ToastMaker {
    suspend fun makeToast(text: String)
    suspend fun makeToast(@StringRes resId: Int)
}

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}