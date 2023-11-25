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

package com.saulhdev.feeder.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.saulhdev.feeder.db.ID_UNSET
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import org.threeten.bp.Instant
import java.net.URL

@Entity(
    tableName = "Feeds",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["id", "url", "title"], unique = true)
    ]
)
class Feed(
    @PrimaryKey(autoGenerate = true)
    var id: Long = ID_UNSET,
    var title: String = "",
    var description: String = "",
    var url: URL = sloppyLinkToStrictURL(""),
    var feedImage: URL = sloppyLinkToStrictURL(""),
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER) var lastSync: Instant = Instant.EPOCH,
    var alternateId: Boolean = false,
    var fullTextByDefault: Boolean = false,
    var tag: String = "",
    var currentlySyncing: Boolean = false,
    var isEnabled: Boolean = true,
)