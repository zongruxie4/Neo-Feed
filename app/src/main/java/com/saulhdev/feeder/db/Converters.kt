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

package com.saulhdev.feeder.db

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import com.saulhdev.feeder.utils.sloppyLinkToStrictURLNoThrows
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import java.lang.reflect.Type
import java.net.URL

class Converters {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun dateTimeFromString(value: String?): ZonedDateTime? {
        var dt: ZonedDateTime? = null
        if (value != null) {
            try {
                dt = ZonedDateTime.parse(value)
            } catch (t: Throwable) {
            }
        }
        return dt
    }

    @TypeConverter
    fun stringFromDateTime(value: ZonedDateTime?): String? =
        value?.toString()

    @TypeConverter
    fun stringFromURL(value: URL?): String? =
        value?.toString()

    @TypeConverter
    fun urlFromString(value: String?): URL? =
        value?.let { sloppyLinkToStrictURLNoThrows(it) }

    @TypeConverter
    fun instantFromLong(value: Long?): Instant? =
        try {
            value?.let { Instant.ofEpochMilli(it) }
        } catch (t: Throwable) {
            null
        }

    @TypeConverter
    fun longFromInstant(value: Instant?): Long? =
        value?.toEpochMilli()

}