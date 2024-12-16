package com.saulhdev.feeder.compose.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


@Composable
operator fun PaddingValues.minus(b: PaddingValues): PaddingValues {
    val a = this
    return remember(a, b) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                val aLeft = a.calculateLeftPadding(layoutDirection)
                val bLeft = b.calculateRightPadding(layoutDirection)
                return (aLeft - bLeft).coerceAtLeast(0.dp)
            }

            override fun calculateTopPadding(): Dp {
                val aTop = a.calculateTopPadding()
                val bTop = b.calculateTopPadding()
                return (aTop - bTop).coerceAtLeast(0.dp)
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                val aRight = a.calculateRightPadding(layoutDirection)
                val bRight = b.calculateRightPadding(layoutDirection)
                return (aRight - bRight).coerceAtLeast(0.dp)
            }

            override fun calculateBottomPadding(): Dp {
                val aBottom = a.calculateBottomPadding()
                val bBottom = b.calculateBottomPadding()
                return (aBottom - bBottom).coerceAtLeast(0.dp)
            }
        }
    }
}

@Composable
operator fun PaddingValues.plus(b: PaddingValues): PaddingValues {
    val a = this
    return remember(a, b) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                val aLeft = a.calculateLeftPadding(layoutDirection)
                val bLeft = b.calculateRightPadding(layoutDirection)
                return (aLeft + bLeft).coerceAtLeast(0.dp)
            }

            override fun calculateTopPadding(): Dp {
                val aTop = a.calculateTopPadding()
                val bTop = b.calculateTopPadding()
                return (aTop + bTop).coerceAtLeast(0.dp)
            }

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                val aRight = a.calculateRightPadding(layoutDirection)
                val bRight = b.calculateRightPadding(layoutDirection)
                return (aRight + bRight).coerceAtLeast(0.dp)
            }

            override fun calculateBottomPadding(): Dp {
                val aBottom = a.calculateBottomPadding()
                val bBottom = b.calculateBottomPadding()
                return (aBottom + bBottom).coerceAtLeast(0.dp)
            }
        }
    }
}