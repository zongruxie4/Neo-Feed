/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.utils

import android.content.Context
import android.text.BidiFormatter
import com.saulhdev.feeder.R
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

fun getThemes(context: Context): Map<String, String> {
    return mapOf(
        "auto_launcher" to context.resources.getString(R.string.theme_auto_launcher),
        "auto_system" to context.resources.getString(R.string.theme_auto_system),
        "light" to context.resources.getString(R.string.theme_light),
        "dark" to context.resources.getString(R.string.theme_dark)
    )
}

fun getSyncFrequency(context: Context): Map<String, String> {
    return mapOf(
        "0.5" to context.resources.getString(R.string.sync_half_hour_minutes),
        "1" to context.resources.getString(R.string.sync_one_hour),
        "2" to context.resources.getString(R.string.sync_two_hours),
        "3" to context.resources.getString(R.string.sync_three_hours),
        "6" to context.resources.getString(R.string.sync_six_hours)
    )
}

fun getItemsPerFeed(): Map<String, String> {
    return mapOf(
        "25" to "25",
        "50" to "50",
        "100" to "100",
        "200" to "200",
        "500" to "500"
    )
}

fun getTransparencyOptions(context: Context): Map<String, String> {
    return mapOf(
        "non_transparent" to context.resources.getString(R.string.transparency_non_transparent),
        "more_half" to context.resources.getString(R.string.transparency_low_transparency),
        "half" to context.resources.getString(R.string.transparency_mid_transparency),
        "less_half" to context.resources.getString(R.string.transparency_high_transparency),
        "transparent" to context.resources.getString(R.string.transparency_transparent)
    )
}

fun getBackgroundOptions(context: Context): Map<String, String> {
    return mapOf(
        "theme" to context.resources.getString(R.string.background_theme_option),
        "white" to context.resources.getString(R.string.background_white),
        "dark" to context.resources.getString(R.string.theme_light),
        "amoled" to context.resources.getString(R.string.background_amoled),
        "launcher_primary" to context.resources.getString(R.string.background_primary_color),
        "launcher_secondary" to context.resources.getString(R.string.background_secondary_color),
        "launcher_tertiary" to context.resources.getString(R.string.background_tertiary_color)
    )
}

/**
 * Ensures a url is valid, having a scheme and everything. It turns 'google.com' into 'http://google.com' for example.
 */
fun sloppyLinkToStrictURL(url: String): URL = try {
    // If no exception, it's valid
    URL(url)
} catch (_: MalformedURLException) {
    URL("http://$url")
}

/**
 * Returns a URL but does not guarantee that it accurately represents the input string if the input string is an invalid URL.
 * This is used to ensure that migrations to versions where Feeds have URL and not strings don't crash.
 */
fun sloppyLinkToStrictURLNoThrows(url: String): URL = try {
    sloppyLinkToStrictURL(url)
} catch (_: MalformedURLException) {
    sloppyLinkToStrictURL("")
}

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsoluteOrNull(base: URL, link: String?): String? = try {
    // If no exception, it's valid
    if (link != null) {
        relativeLinkIntoAbsoluteOrThrow(base, link).toString()
    } else {
        null
    }
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsolute(base: URL, link: String): String = try {
    // If no exception, it's valid
    relativeLinkIntoAbsoluteOrThrow(base, link).toString()
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, throws MalformedURLException.
 */
@Throws(MalformedURLException::class)
fun relativeLinkIntoAbsoluteOrThrow(base: URL, link: String): URL = try {
    // If no exception, it's valid
    URL(link)
} catch (_: MalformedURLException) {
    URL(base, link)
}

private val regexImgSrc = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)

fun naiveFindImageLink(text: String?): String? =
    if (text != null) {
        val imgLink = regexImgSrc.find(text)?.groupValues?.get(2)
        if (imgLink?.contains("twitter_icon", ignoreCase = true) == true) {
            null
        } else {
            imgLink
        }
    } else {
        null
    }

fun String.urlEncode(): String =
    URLEncoder.encode(this, "UTF-8")

fun String.urlDecode(): String =
    URLDecoder.decode(this, "UTF-8")

fun Context.unicodeWrap(text: String): String =
    BidiFormatter.getInstance(getLocale()).unicodeWrap(text)

fun Context.getLocale(): Locale =
    resources.configuration.locales[0]
