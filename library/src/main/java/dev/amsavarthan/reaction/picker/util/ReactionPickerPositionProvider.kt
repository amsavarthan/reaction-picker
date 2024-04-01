package dev.amsavarthan.reaction.picker.util

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

/**
 * Calculates the position of the reaction picker on the screen.
 *
 * @author Amsavarthan Lv
 */
@Immutable
interface ReactionPickerPositionProvider {

    /**
     * Calculates the position of the reaction picker on the screen.
     *
     * @param anchorBounds The bounds of the anchor view.
     * @param windowSize The size of the window.
     * @param layoutDirection The layout direction of the anchor view.
     * @param popupContentSize The size of the reaction picker.
     * @param anchorTappedPosition The position where the user tapped on the anchor view.
     *
     * @return The position of the reaction picker on the screen.
     */
    fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
        anchorTappedPosition: IntOffset,
    ): IntOffset

}