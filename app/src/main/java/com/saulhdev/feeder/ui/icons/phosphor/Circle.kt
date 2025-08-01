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

val Phosphor.Circle: ImageVector
    get() {
        if (_circle != null) {
            return _circle!!
        }
        _circle = Builder(
            name = "Circle",
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
                moveTo(128.0f, 232.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, true, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, true, 128.0f, 232.0f)
                close()
                moveTo(128.0f, 40.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, false, 88.0f, 88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, false, 128.0f, 40.0f)
                close()
            }
        }
            .build()
        return _circle!!
    }

private var _circle: ImageVector? = null