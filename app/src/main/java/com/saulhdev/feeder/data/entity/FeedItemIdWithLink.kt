/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.saulhdev.feeder.data.entity

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.data.db.models.FeedItemForFetching

data class FeedItemIdWithLink @Ignore constructor(
    @ColumnInfo(name = "id") override var id: Long = ID_UNSET,
    @ColumnInfo(name = "link") override var link: String? = null,
) : FeedItemForFetching {
    constructor() : this(id = ID_UNSET)
}
