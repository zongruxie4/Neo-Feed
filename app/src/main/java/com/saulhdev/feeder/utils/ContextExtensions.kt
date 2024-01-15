package com.saulhdev.feeder.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri

interface ToastMaker {
    suspend fun makeToast(text: String)
    suspend fun makeToast(@StringRes resId: Int)
}

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.launchView(url: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        )
    )
}

fun Context.shareIntent(url: String, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, title)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)

    startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
}
