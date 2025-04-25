package com.saulhdev.feeder.ui.compose.icon.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.ui.compose.icon.Phosphor

val Phosphor.DotsThreeVertical: ImageVector
    get() {
        if (_dotsThreeVertical != null) {
            return _dotsThreeVertical!!
        }
        _dotsThreeVertical = ImageVector.Builder(
            name = "DotsThreeVertical",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(140f, 128f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, -12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 140f, 128f)
                close()
                moveTo(128f, 72f)
                arcToRelative(
                    12f,
                    12f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -12f,
                    -12f
                )
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 128f, 72f)
                close()
                moveTo(128f, 184f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 12f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 128f, 184f)
                close()
            }
        }.build()

        return _dotsThreeVertical!!
    }

private var _dotsThreeVertical: ImageVector? = null
