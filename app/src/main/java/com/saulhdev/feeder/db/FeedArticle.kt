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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime

@Entity(
    tableName = "FeedArticle",
    indices = [
        Index(value = ["id", "feedId"], unique = true),
        Index(value = ["feedId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = CASCADE
        )
    ]
)
data class FeedArticle(
    @PrimaryKey(autoGenerate = true)
    var id: Long = ID_UNSET,
    var guid: String = "",
    var title: String = "",
    var plainTitle: String = "",
    var imageUrl: String? = null,
    var enclosureLink: String? = null,
    var plainSnippet: String = "",
    val description: String = "",
    val content_html: String = "",
    var author: String? = "",
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) var pubDate: ZonedDateTime? = null,
    var link: String? = "",
    var feedId: Long = 0,
    val categories: ArrayList<String> = arrayListOf()
)