/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.ui.compose.icon.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.compose.icon.Phosphor

val Phosphor.EyedropperSample: ImageVector
    get() {
        if (_eyedropper_sample != null) {
            return _eyedropper_sample!!
        }
        _eyedropper_sample = Builder(
            name = "Eyedropper-sample",
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
                moveTo(224.0f, 67.5f)
                arcToRelative(35.5f, 35.5f, 0.0f, false, false, -11.3f, -25.7f)
                curveToRelative(-14.0f, -13.2f, -36.7f, -12.7f, -50.6f, 1.2f)
                lineTo(140.2f, 64.9f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, -33.2f, 0.8f)
                lineToRelative(-7.0f, 7.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, false, 0.0f, 22.6f)
                lineToRelative(2.1f, 2.1f)
                lineTo(51.7f, 147.7f)
                arcToRelative(40.3f, 40.3f, 0.0f, false, false, -11.0f, 35.8f)
                lineToRelative(-9.8f, 22.4f)
                arcToRelative(13.6f, 13.6f, 0.0f, false, false, 2.9f, 15.2f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, false, 11.3f, 4.7f)
                arcToRelative(16.3f, 16.3f, 0.0f, false, false, 6.4f, -1.3f)
                lineToRelative(21.0f, -9.2f)
                arcToRelative(40.3f, 40.3f, 0.0f, false, false, 35.8f, -11.0f)
                lineToRelative(50.3f, -50.4f)
                lineToRelative(2.1f, 2.1f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 22.6f, 0.0f)
                lineToRelative(7.0f, -7.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 0.8f, -33.2f)
                lineToRelative(22.4f, -22.3f)
                arcTo(36.2f, 36.2f, 0.0f, false, false, 224.0f, 67.5f)
                close()
                moveTo(97.0f, 193.0f)
                arcToRelative(24.3f, 24.3f, 0.0f, false, true, -23.2f, 6.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -5.3f, 0.4f)
                lineTo(47.0f, 209.0f)
                lineToRelative(9.4f, -21.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.4f, -5.3f)
                arcToRelative(23.7f, 23.7f, 0.0f, false, true, 0.6f, -14.2f)
                horizontalLineToRelative(64.5f)
                close()
                moveTo(137.9f, 152.0f)
                lineTo(70.1f, 152.0f)
                lineToRelative(43.3f, -43.3f)
                lineToRelative(33.9f, 33.9f)
                close()
                moveTo(202.1f, 82.1f)
                lineTo(174.1f, 110.1f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                lineToRelative(4.9f, 4.8f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-7.0f, 7.0f)
                lineTo(111.3f, 84.0f)
                lineToRelative(7.0f, -7.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.4f, 0.0f)
                lineToRelative(4.8f, 4.9f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(27.5f, -27.6f)
                curveToRelative(7.9f, -7.8f, 20.6f, -8.2f, 28.3f, -0.8f)
                arcTo(19.7f, 19.7f, 0.0f, false, true, 208.0f, 67.7f)
                arcTo(19.4f, 19.4f, 0.0f, false, true, 202.1f, 82.1f)
                close()
            }
        }
            .build()
        return _eyedropper_sample!!
    }

private var _eyedropper_sample: ImageVector? = null
