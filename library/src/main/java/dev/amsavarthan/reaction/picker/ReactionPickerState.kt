package dev.amsavarthan.reaction.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.amsavarthan.reaction.picker.enums.Placement
import dev.amsavarthan.reaction.picker.models.Reaction

/**
 * Create a new [ReactionPickerState] that will be remembered across compositions.
 *
 * @param reactions The list of reactions to be displayed in the picker. At least one reaction should be provided.
 * @param initialSelection The initial reaction to be selected.
 * @param onReacted The callback to be invoked when a reaction is changed.
 *
 * @author Amsavarthan Lv
 */
@Composable
fun rememberReactionPickerState(
    reactions: List<Reaction>,
    initialSelection: Reaction?,
    onReacted: (Reaction?) -> Unit = {},
): ReactionPickerState {
    check(reactions.isNotEmpty()) {
        "At least one reaction should be provided"
    }
    return remember(reactions, onReacted) {
        ReactionPickerStateImpl(
            reactions = reactions,
            initialSelection = initialSelection,
            onReacted = onReacted
        )
    }
}

/**
 * Represents the state of a reaction picker.
 *
 * @author Amsavarthan Lv
 */
@Stable
interface ReactionPickerState {

    /**
     * List of reactions available for selection.
     */
    val reactions: List<Reaction>

    /**
     * Current visibility status of the picker.
     */
    val isVisible: Boolean

    /**
     * Flag indicating whether the picker is being dismissed with a selection.
     */
    val isDismissingWithSelection: Boolean

    /**
     * Flag indicating whether dragging is enabled.
     */
    val canDrag: Boolean

    /**
     * Initial position where the picker was activated.
     */
    val triggerPosition: Offset

    /**
     * Current position of the user's pointer.
     */
    val pointerPosition: Offset

    /**
     * Reaction item currently under the pointer.
     */
    val hoveredReaction: Reaction?

    /**
     * Reaction item that is currently selected.
     */
    val selectedReaction: Reaction?

    /**
     * Bounds of the anchor view.
     */
    var anchorBounds: Rect

    /**
     * Bounds of the picker view.
     */
    var pickerBounds: Rect

    /**
     * Configuration for picker placement.
     */
    var pickerPlacement: Placement

    /**
     * Display the picker at a specific position or an unspecified one.
     *
     * @param triggerPosition The position where the picker was activated.
     */
    fun show(triggerPosition: Offset = Offset.Unspecified)

    /**
     * Get the reaction item at a given pointer position.
     *
     * @param pointerPosition The position of the user's pointer.
     * @return The reaction item at the given pointer position.
     */
    fun getReaction(pointerPosition: Offset): Reaction?

    /**
     * Update bounds of a reaction item at a specific index.
     *
     * @param index The index of the reaction item.
     * @param bounds The updated bounds for the reaction item.
     */
    fun updateReactionItemBounds(index: Int, bounds: Rect)

    /**
     * Hide and dismiss the picker, optionally with a selected reaction.
     *
     * @param reaction The selected reaction to dismiss with.
     */
    fun dismiss(reaction: Reaction? = null)

    /**
     * Track and update the position of the user's pointer.
     *
     * @param position The current position of the user's pointer.
     */
    fun trackPointer(position: Offset)

    /**
     * Start tracking the user's pointer.
     */
    fun enablePointerTracking()

    /**
     * Stop tracking the user's pointer.
     */
    fun disablePointerTracking()

    /**
     * Toggle the selection of a reaction. If no reaction is provided, the first reaction will be selected.
     */
    fun toggleReaction(reaction: Reaction = reactions.first())

    /**
     * Invalidate the current selection.
     */
    fun invalidateReaction()

    /**
     * React to a given reaction.
     *
     * @param reaction The reaction to be reacted to. If null, the first reaction will be selected.
     */
    fun react(reaction: Reaction)

    /**
     * Dispose the picker state.
     */
    fun dispose()

}

@Stable
internal class ReactionPickerStateImpl internal constructor(
    initialSelection: Reaction?,
    override val reactions: List<Reaction>,
    private val onReacted: (Reaction?) -> Unit,
) : ReactionPickerState {

    override var isVisible by mutableStateOf(false)

    override var isDismissingWithSelection by mutableStateOf(false)

    override var canDrag by mutableStateOf(false)

    override var triggerPosition by mutableStateOf(Offset.Unspecified)

    override var pointerPosition by mutableStateOf(Offset.Unspecified)

    override var hoveredReaction by mutableStateOf<Reaction?>(null)

    override var selectedReaction by mutableStateOf<Reaction?>(initialSelection)

    override var anchorBounds by mutableStateOf(Rect.Zero)

    override var pickerBounds by mutableStateOf(Rect.Zero)

    override var pickerPlacement by mutableStateOf(Placement.ABOVE)

    private val reactionItemBounds = mutableStateListOf<Rect>()

    override fun show(triggerPosition: Offset) {
        isVisible = true
        this.triggerPosition = triggerPosition
    }

    override fun getReaction(pointerPosition: Offset): Reaction? {
        val index = reactionItemBounds.indexOfFirst { bounds ->
            pointerPosition.x in bounds.left..bounds.right &&
                    pointerPosition.y in pickerBounds.top..pickerBounds.bottom
        }
        return reactions.getOrNull(index)
    }

    override fun dismiss(reaction: Reaction?) {
        if (reaction != null) react(reaction)

        isDismissingWithSelection = reaction != null
        isVisible = false
        invalidatePointer()
    }

    // Reset the pointer position and hovered reaction
    private fun invalidatePointer() {
        disablePointerTracking()
        hoveredReaction = null
    }

    override fun updateReactionItemBounds(index: Int, bounds: Rect) {
        if (reactionItemBounds.size > index) {
            reactionItemBounds[index] = bounds
        } else {
            reactionItemBounds.add(index, bounds)
        }
    }

    override fun trackPointer(position: Offset) {
        if (!canDrag) return
        //dragStarted = true
        pointerPosition = position

        // Update the hovered reaction
        if (position == Offset.Unspecified) return
        hoveredReaction = getReaction(position)
    }

    override fun enablePointerTracking() {
        canDrag = true
    }

    override fun disablePointerTracking() {
        canDrag = false
        pointerPosition = Offset.Unspecified
    }

    override fun toggleReaction(reaction: Reaction) {
        if (selectedReaction == null) {
            react(reaction)
        } else {
            invalidateReaction()
        }
    }

    override fun react(reaction: Reaction) {
        selectedReaction = reaction
        onReacted(reaction)
    }

    override fun invalidateReaction() {
        selectedReaction = null
        onReacted(null)
    }

    override fun dispose() {
        dismiss()
        reactionItemBounds.clear()
    }

}

internal val LocalReactionPickerStateHolder = compositionLocalOf<ReactionPickerStateHolder> {
    error("Cannot attach reaction picker. Add ReactionPickerLayout to the composition tree.")
}

@Stable
internal interface ReactionPickerStateHolder {

    /**
     * The state of the available picker in the composition tree.
     */
    val pickerState: ReactionPickerState?

    /**
     * Indicates whether a picker is available.
     */
    val isPickerAvailable: Boolean
        get() = pickerState != null

    /**
     * Updates the state of the current available picker.
     */
    fun updatePickerState(state: ReactionPickerState?)

}

@Stable
internal class ReactionPickerStateHolderImpl : ReactionPickerStateHolder {

    override var pickerState by mutableStateOf<ReactionPickerState?>(null)

    override fun updatePickerState(state: ReactionPickerState?) {
        pickerState = state
    }

}