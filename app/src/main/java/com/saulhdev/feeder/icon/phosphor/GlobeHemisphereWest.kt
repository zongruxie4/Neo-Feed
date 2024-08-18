/*
 * This file is part of Neo Feed
 * Copyright (c) 2024   Neo Applications Team
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

package com.saulhdev.feeder.icon.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.icon.Phosphor

val Phosphor.GlobeHemisphereWest: ImageVector
    get() {
        if (_clock != null) {
            return _clock!!
        }
        _clock = Builder(
            name = "Clock",
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
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.11f, 104.11f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(216.0f, 128.0f)
                arcTo(87.62f, 87.62f, 0.0f, false, true, 209.6f, 160.94f)
                lineTo(164.9f, 133.45f)
                arcTo(15.92f, 15.92f, 0.0f, false, false, 158.66f, 131.22f)
                lineTo(135.84f, 128.14f)
                arcTo(16.11f, 16.11f, 0.0f, false, false, 119.0f, 136.0f)
                horizontalLineTo(110.28f)
                lineTo(106.48f, 128.14f)
                arcTo(15.91f, 15.91f, 0.0f, false, false, 95.0f, 119.47f)
                lineTo(87.0f, 117.74f)
                lineTo(79.0f, 116.01f)
                lineTo(96.14f, 104.0f)
                horizontalLineTo(112.85f)
                arcTo(16.06f, 16.06f, 0.0f, false, false, 120.58f, 102.0f)
                lineTo(132.83f, 95.24f)
                arcTo(16.62f, 16.62f, 0.0f, false, false, 135.83f, 93.1f)
                lineTo(162.74f, 68.76f)
                arcTo(15.93f, 15.93f, 0.0f, false, false, 166.0f, 49.1f)
                lineTo(165.64f, 48.45f)
                arcTo(88.11f, 88.11f, 0.0f, false, true, 216.0f, 128.0f)
                close()
                moveTo(143.31f, 41.34f)
                lineTo(152.0f, 56.9f)
                lineTo(125.09f, 81.24f)
                lineTo(112.85f, 88.0f)
                horizontalLineTo(96.14f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 82.26f, 96.0f)
                lineTo(73.53f, 111.23f)
                lineTo(64.8f, 84.19f)
                lineTo(74.32f, 58.32f)
                arcTo(87.87f, 87.87f, 0.0f, false, true, 143.31f, 41.34f)
                close()
                moveTo(40.0f, 128.0f)
                arcTo(87.53f, 87.53f, 0.0f, false, true, 48.54f, 90.2f)
                lineTo(59.88f, 120.47f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 71.5f, 130.0f)
                lineTo(92.93f, 134.61f)
                lineTo(96.74f, 143.0f)
                arcTo(16.09f, 16.09f, 0.0f, false, false, 111.14f, 152.0f)
                horizontalLineTo(112.62f)
                lineTo(105.39f, 168.23f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 108.25f, 185.6f)
                lineTo(108.39f, 185.74f)
                lineTo(128.0f, 205.94f)
                lineTo(126.06f, 215.0f)
                arcTo(88.11f, 88.11f, 0.0f, false, true, 40.0f, 128.0f)
                close()
                moveTo(142.58f, 214.78f)
                lineTo(143.71f, 208.97f)
                arcTo(16.09f, 16.09f, 0.0f, false, false, 139.71f, 195.07f)
                lineTo(120.0f, 174.74f)
                lineTo(133.7f, 144.0f)
                lineTo(156.52f, 147.08f)
                lineTo(202.24f, 175.2f)
                arcTo(88.18f, 88.18f, 0.0f, false, true, 142.58f, 214.78f)
                close()

            }
        }
            .build()
        return _clock!!
    }

private var _clock: ImageVector? = null