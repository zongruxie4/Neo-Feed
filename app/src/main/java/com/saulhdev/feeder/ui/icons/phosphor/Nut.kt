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

val Phosphor.Nut: ImageVector
    get() {
        if (_nut != null) {
            return _nut!!
        }
        _nut = Builder(
            name = "Nut",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(219.9f, 66.7f)
                lineToRelative(-84.0f, -47.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, -15.8f, 0.0f)
                lineToRelative(-84.0f, 47.4f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, -8.1f, 14.0f)
                verticalLineToRelative(94.6f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, 8.1f, 14.0f)
                lineToRelative(84.0f, 47.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 15.8f, 0.0f)
                lineToRelative(84.0f, -47.4f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, 8.1f, -14.0f)
                lineTo(228.0f, 80.7f)
                arcTo(16.2f, 16.2f, 0.0f, false, false, 219.9f, 66.7f)
                close()
                moveTo(212.0f, 175.3f)
                lineToRelative(-84.0f, 47.5f)
                lineTo(44.0f, 175.3f)
                lineTo(44.0f, 80.7f)
                lineToRelative(84.0f, -47.5f)
                lineToRelative(84.0f, 47.5f)
                close()
                moveTo(84.0f, 128.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, true, false, 44.0f, -44.0f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 84.0f, 128.0f)
                close()
                moveTo(156.0f, 128.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, true, true, -28.0f, -28.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, true, 156.0f, 128.0f)
                close()
            }
        }
            .build()
        return _nut!!
    }

private var _nut: ImageVector? = null
