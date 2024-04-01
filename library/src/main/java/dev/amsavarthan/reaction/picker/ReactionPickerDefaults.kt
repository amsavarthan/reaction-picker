package dev.amsavarthan.reaction.picker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import dev.amsavarthan.reaction.picker.util.ReactionPickerPositionProvider

object ReactionPickerDefaults {

    @Composable
    fun rememberReactionPickerPositionProvider(
        spacingBetweenReactionPickerAndAnchor: Dp = SpacingBetweenReactionPickerAndAnchor,
    ): ReactionPickerPositionProvider {
        val anchorSpacing = with(LocalDensity.current) {
            spacingBetweenReactionPickerAndAnchor.roundToPx()
        }
        return remember(anchorSpacing) {
            object : ReactionPickerPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                    anchorTappedPosition: IntOffset,
                ): IntOffset {
                    // Place at start of the tapped position
                    var xPos = anchorBounds.left + anchorTappedPosition.x

                    // Move to the left if it goes out of the screen
                    if (xPos + popupContentSize.width > windowSize.width) {
                        xPos = windowSize.width - popupContentSize.width - anchorSpacing
                    }

                    // Place above the anchor
                    var yPos =
                        anchorBounds.top - popupContentSize.height - anchorSpacing

                    // Move below the anchor if it goes out of the screen
                    if (yPos < 0) {
                        yPos = anchorBounds.bottom + anchorSpacing
                    }

                    return IntOffset(xPos, yPos)
                }
            }
        }
    }

}

internal val SpacingBetweenReactionPickerAndAnchor = 4.dp
internal val SpacingBetweenReactionItemAndLabel = 8.dp
internal val SpacingBetweenReactions = 4.dp

internal const val ReactionPickerHeightFactor = 1.6f
internal val ReactionPickerPadding = PaddingValues(8.dp)

internal val ReactionItemSize = 40.dp
internal val ReactionItemSizeOnAnyHover = 34.dp
internal val ReactionItemSizeOnHover = 72.dp
