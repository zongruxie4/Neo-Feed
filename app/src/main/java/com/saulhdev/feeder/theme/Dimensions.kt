package com.saulhdev.feeder.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
class Dimensions(
    /**
     * Margin of the navigation button in app bar
     */
    val navIconMargin: Dp,
    /**
     * A gutter is the space between columns that helps separate content.
     */
    val gutter: Dp,
    /**
     * Margins are the space between content and the left and right edges of the screen.
     */
    val margin: Dp,
    /**
     * The max width of the content in case of very wide screens.
     */
    val maxContentWidth: Dp,
    /**
     * The responsive column grid is made up of columns, gutters, and margins, providing a
     * convenient structure for the layout of elements within the body region.
     * Components, imagery, and text align with the column grid to ensure a logical and
     * consistent layout across screen sizes and orientations.
     *
     * As the size of the body region grows or shrinks, the number of grid columns
     * changes in response.
     */
    val layoutColumns: Int,
    /**
     * Number of columns in feed screen
     */
    val feedScreenColumns: Int
)

val phoneDimensions = Dimensions(
    maxContentWidth = 840.dp,
    navIconMargin = 16.dp,
    margin = 16.dp,
    gutter = 16.dp,
    layoutColumns = 4,
    feedScreenColumns = 1,
)

val LocalDimens = staticCompositionLocalOf {
    phoneDimensions
}
