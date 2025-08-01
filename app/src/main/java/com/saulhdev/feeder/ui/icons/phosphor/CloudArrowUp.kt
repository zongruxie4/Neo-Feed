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

val Phosphor.CloudArrowUp: ImageVector
    get() {
        if (_cloud_arrow_up != null) {
            return _cloud_arrow_up!!
        }
        _cloud_arrow_up = Builder(
            name = "Cloud-arrow-up",
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
                moveTo(248.0f, 128.0f)
                arcToRelative(87.3f, 87.3f, 0.0f, false, true, -17.6f, 52.8f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 184.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -4.8f, -1.6f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -1.6f, -11.2f)
                arcTo(72.0f, 72.0f, 0.0f, true, false, 88.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                arcToRelative(85.7f, 85.7f, 0.0f, false, true, 3.3f, -23.9f)
                lineTo(72.0f, 104.1f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 0.0f, 96.0f)
                lineTo(96.0f, 200.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(72.0f, 216.1f)
                arcTo(64.0f, 64.0f, 0.0f, false, true, 72.0f, 88.0f)
                arcToRelative(58.2f, 58.2f, 0.0f, false, true, 9.3f, 0.7f)
                arcTo(88.0f, 88.0f, 0.0f, false, true, 248.0f, 128.0f)
                close()
                moveTo(157.7f, 122.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 0.0f)
                lineToRelative(-33.9f, 34.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 11.3f, 11.3f)
                lineTo(144.0f, 147.3f)
                lineTo(144.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(160.0f, 147.3f)
                lineToRelative(20.3f, 20.3f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, false, 5.6f, 2.3f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 5.7f, -2.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -11.3f)
                close()
            }
        }
            .build()
        return _cloud_arrow_up!!
    }

private var _cloud_arrow_up: ImageVector? = null
