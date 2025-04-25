package com.saulhdev.feeder.ui.compose.icon.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.compose.icon.Phosphor

val Phosphor.ArrowCounterClockwise: ImageVector
    get() {
        if (_arrowCounterClockwise != null) {
            return _arrowCounterClockwise!!
        }
        _arrowCounterClockwise = ImageVector.Builder(
            name = "ArrowCounterClockwise",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(224f, 128f)
                arcToRelative(
                    96f,
                    96f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -94.71f,
                    96f
                )
                horizontalLineTo(128f)
                arcTo(
                    95.38f,
                    95.38f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    62.1f,
                    197.8f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    11f,
                    -11.63f
                )
                arcTo(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = false, 71.43f, 71.39f)
                arcToRelative(
                    3.07f,
                    3.07f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -0.26f,
                    0.25f
                )
                lineTo(44.59f, 96f)
                horizontalLineTo(72f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                horizontalLineTo(24f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, -8f)
                verticalLineTo(56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 0f)
                verticalLineTo(85.8f)
                lineTo(60.25f, 60f)
                arcTo(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 128f)
                close()
            }
        }.build()

        return _arrowCounterClockwise!!
    }

private var _arrowCounterClockwise: ImageVector? = null
