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

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.icons.Phosphor

val Phosphor.ShareNetwork: ImageVector
    get() {
        if (_share_network != null) {
            return _share_network!!
        }
        _share_network = Builder(
            name = "Share-network",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(176.0f, 160.0f)
                arcToRelative(39.7f, 39.7f, 0.0f, false, false, -28.6f, 12.1f)
                lineToRelative(-46.1f, -29.6f)
                arcToRelative(40.3f, 40.3f, 0.0f, false, false, 0.0f, -29.0f)
                lineToRelative(46.1f, -29.6f)
                arcTo(40.0f, 40.0f, 0.0f, true, false, 136.0f, 56.0f)
                arcToRelative(41.0f, 41.0f, 0.0f, false, false, 2.7f, 14.5f)
                lineTo(92.6f, 100.1f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, 0.0f, 55.8f)
                lineToRelative(46.1f, 29.6f)
                arcTo(41.0f, 41.0f, 0.0f, false, false, 136.0f, 200.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, 40.0f, -40.0f)
                close()
                moveTo(176.0f, 32.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, -24.0f, 24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 176.0f, 32.0f)
                close()
                moveTo(64.0f, 152.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 64.0f, 152.0f)
                close()
                moveTo(176.0f, 224.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 176.0f, 224.0f)
                close()
            }
        }
            .build()
        return _share_network!!
    }

private var _share_network: ImageVector? = null


@Preview
@Composable
fun ShareNetworkPreview() {
    Image(
        Phosphor.ShareNetwork,
        null
    )
}
