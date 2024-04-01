package dev.amsavarthan.reaction.picker.models

import androidx.compose.ui.unit.Dp
import dev.amsavarthan.reaction.picker.ReactionItemSize
import dev.amsavarthan.reaction.picker.ReactionItemSizeOnAnyHover
import dev.amsavarthan.reaction.picker.ReactionItemSizeOnHover
import dev.amsavarthan.reaction.picker.SpacingBetweenReactionItemAndLabel
import dev.amsavarthan.reaction.picker.SpacingBetweenReactions

/**
 * Properties for the reaction picker.
 *
 * @param idleReactionSize The size of the reaction item when it is idle.
 * @param activeReactionSize The size of the reaction item when it is active or hovered.
 * @param inActiveReactionSize The size of the reaction item when any item is hovered.
 * @param spaceBetweenReactions The spacing between individual reaction items.
 * @param spaceBetweenReactionAndLabel The spacing between a reaction item and its label.
 * @param hapticFeedbackEnabled Determines whether haptic feedback should be enabled.
 * @param dismissOnBackPress Determines whether the reaction picker should dismiss when the back button is pressed.
 *
 * @author Amsavarthan Lv
 */
data class ReactionPickerProperties(
    val idleReactionSize: Dp = ReactionItemSize,
    val activeReactionSize: Dp = ReactionItemSizeOnHover,
    val inActiveReactionSize: Dp = ReactionItemSizeOnAnyHover,
    val spaceBetweenReactions: Dp = SpacingBetweenReactions,
    val spaceBetweenReactionAndLabel: Dp = SpacingBetweenReactionItemAndLabel,
    val hapticFeedbackEnabled: Boolean = true,
    val dismissOnBackPress: Boolean = true,
)