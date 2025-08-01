/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Neo Feed Team
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

package com.saulhdev.feeder.ui.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.icons.Phosphor

val Phosphor.Bookmarks: ImageVector
    get() {
        if (_bookmarks != null) {
            return _bookmarks!!
        }
        _bookmarks = Builder(
            name = "Bookmarks",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(192.0f, 24.0f)
                lineTo(96.0f, 24.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 80.0f, 40.0f)
                lineTo(80.0f, 56.0f)
                lineTo(64.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 72.0f)
                lineTo(48.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 4.7f, -1.5f)
                lineTo(112.0f, 193.8f)
                lineToRelative(51.4f, 36.7f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 8.3f, 0.6f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 176.0f, 224.0f)
                lineTo(176.0f, 184.7f)
                lineToRelative(19.4f, 13.8f)
                arcTo(7.7f, 7.7f, 0.0f, false, false, 200.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(208.0f, 40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 192.0f, 24.0f)
                close()
                moveTo(160.0f, 208.5f)
                lineToRelative(-43.4f, -31.0f)
                arcTo(7.7f, 7.7f, 0.0f, false, false, 112.0f, 176.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -4.7f, 1.5f)
                lineTo(64.0f, 208.5f)
                lineTo(64.0f, 72.0f)
                horizontalLineToRelative(96.0f)
                close()
                moveTo(192.0f, 176.5f)
                lineTo(176.0f, 165.0f)
                lineTo(176.0f, 72.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(96.0f, 56.0f)
                lineTo(96.0f, 40.0f)
                horizontalLineToRelative(96.0f)
                close()
            }
        }
            .build()
        return _bookmarks!!
    }

private var _bookmarks: ImageVector? = null
