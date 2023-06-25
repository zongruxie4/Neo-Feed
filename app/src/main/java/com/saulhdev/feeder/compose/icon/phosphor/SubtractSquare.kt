package com.saulhdev.feeder.compose.icon.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.compose.icon.Phosphor

public val Phosphor.SubtractSquare: ImageVector
    get() {
        if (_subtract_square != null) {
            return _subtract_square!!
        }
        _subtract_square = Builder(
            name = "Subtract-square",
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
                moveTo(224.0f, 160.0f)
                lineTo(224.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(168.0f, 88.0f)
                lineTo(168.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(40.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(32.0f, 160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(88.0f, 168.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(216.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(224.0f, 160.0f)
                close()
                moveTo(163.31f, 208.0f)
                lineTo(123.31f, 168.0f)
                horizontalLineToRelative(33.38f)
                lineToRelative(40.0f, 40.0f)
                close()
                moveTo(168.0f, 156.69f)
                lineTo(168.0f, 123.31f)
                lineToRelative(40.0f, 40.0f)
                verticalLineToRelative(33.38f)
                close()
                moveTo(208.0f, 140.69f)
                lineTo(171.31f, 104.0f)
                lineTo(208.0f, 104.0f)
                close()
                moveTo(48.0f, 48.0f)
                lineTo(152.0f, 48.0f)
                verticalLineToRelative(56.0f)
                horizontalLineToRelative(0.0f)
                verticalLineToRelative(48.0f)
                lineTo(48.0f, 152.0f)
                close()
                moveTo(104.0f, 171.31f)
                lineTo(140.69f, 208.0f)
                lineTo(104.0f, 208.0f)
                close()
            }
        }
            .build()
        return _subtract_square!!
    }

private var _subtract_square: ImageVector? = null
