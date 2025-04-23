package com.saulhdev.feeder.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.saulhdev.feeder.data.db.ID_UNSET

data class FeedItemIdWithLink @Ignore constructor(
    @ColumnInfo(name = "id") override var id: Long = ID_UNSET,
    @ColumnInfo(name = "link") override var link: String? = null,
) : FeedItemForFetching {
    constructor() : this(id = ID_UNSET)
}
