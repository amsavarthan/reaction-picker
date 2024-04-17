package dev.amsavarthan.reaction.picker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.zIndex
import dev.amsavarthan.reaction.picker.enums.Placement
import dev.amsavarthan.reaction.picker.models.Reaction
import dev.amsavarthan.reaction.picker.models.ReactionPickerProperties
import dev.amsavarthan.reaction.picker.util.Duration
import dev.amsavarthan.reaction.picker.util.ReactionPickerPositionProvider
import dev.amsavarthan.reaction.picker.util.bezierAnimation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

/**
 * Layout that wraps the content and shows a reaction picker when the anchor is long pressed.
 * The anchor can be any composable that has the [reactionPickerAnchor] modifier in its modifier chain.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param positionProvider Provides the screen position for reaction picker.
 * @param properties The properties to customize the reaction picker.
 * @param containerColor The color of the reaction picker container.
 * @param containerShape The shape of the reaction picker container.
 * @param label The composable for label of a reaction item.
 * @param icon The composable for icon of a reaction item.
 * @param content The content to be wrapped by the layout.
 *
 * @author Amsavarthan Lv
 */
@Composable
fun ReactionPickerLayout(
    modifier: Modifier = Modifier,
    positionProvider: ReactionPickerPositionProvider = ReactionPickerDefaults.rememberReactionPickerPositionProvider(),
    properties: ReactionPickerProperties = ReactionPickerProperties(),
    containerColor: Color = Color.Black,
    containerShape: Shape = CircleShape,
    label: @Composable (String) -> Unit = { text ->
        ReactionLabel(text = text)
    },
    icon: @Composable (Reaction) -> Unit = { reaction ->
        ReactionIcon(
            modifier = Modifier.fillMaxSize(),
            reaction = reaction
        )
    },
    content: @Composable () -> Unit,
) {
    val stateHolder = remember { ReactionPickerStateHolderImpl() }

    CompositionLocalProvider(
        LocalReactionPickerStateHolder provides stateHolder
    ) {
        Box {
            if (stateHolder.isPickerAvailable) {
                val pickerState = stateHolder.pickerState!!
                ReactionPicker(
                    state = pickerState,
                    positionProvider = positionProvider,
                    properties = properties,
                    label = label,
                    icon = icon,
                    containerColor = containerColor,
                    containerShape = containerShape,
                    onDismiss = { stateHolder.updatePickerState(null) }
                )
            }

            WrappedContent(
                modifier = modifier,
                state = stateHolder.pickerState,
                content = content
            )
        }
    }
}

@Composable
fun ReactionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}

@Composable
private fun WrappedContent(
    state: ReactionPickerState?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .handleGesture(state),
    ) { content() }
}

@Composable
private fun ReactionPicker(
    state: ReactionPickerState,
    positionProvider: ReactionPickerPositionProvider,
    properties: ReactionPickerProperties,
    containerColor: Color,
    containerShape: Shape,
    onDismiss: () -> Unit,
    label: @Composable (String) -> Unit,
    icon: @Composable (Reaction) -> Unit,
) {

    val currentOnDismiss by rememberUpdatedState(onDismiss)

    DisposableEffect(Unit) {
        onDispose { state.dispose() }
    }

    // Perform haptic on hover if enabled
    val haptic = LocalHapticFeedback.current
    if (properties.hapticFeedbackEnabled) {
        LaunchedEffect(state.hoveredReaction) {
            if (state.hoveredReaction == null) return@LaunchedEffect
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Ensure that the pointer position is tracked
    LaunchedEffect(state.pointerPosition) {
        state.trackPointer(state.pointerPosition)
    }

    // Additional state picker visibility to handle the animation
    var animatedVisibility by remember { mutableStateOf(false) }

    // This side effect ensures that the picker is shown
    LaunchedEffect(state.isVisible) {
        if (!state.isVisible) return@LaunchedEffect
        animatedVisibility = state.isVisible
    }

    // This side effect ensures that the picker is dismissed after animation
    LaunchedEffect(animatedVisibility) {
        if (animatedVisibility) return@LaunchedEffect
        currentOnDismiss()
    }

    val view = LocalView.current
    val layoutDirection = LocalLayoutDirection.current

    var contentSize by remember { mutableStateOf(IntSize.Zero) }

    val anchorBounds = state.anchorBounds
    val verticalAlignment = remember(state.pickerPlacement) {
        when (state.pickerPlacement) {
            Placement.ABOVE -> Alignment.Bottom
            Placement.BELOW -> Alignment.Top
        }
    }

    Box(
        modifier = Modifier
            .zIndex(Float.MAX_VALUE)
            .width(IntrinsicSize.Min)
            .height(properties.activeReactionSize * ReactionPickerHeightFactor)
            .onSizeChanged {
                // Update the content size when the size changes
                contentSize = it
            }
            .offset {
                val windowSize = IntSize(view.width, view.height)
                val offset = positionProvider.calculatePosition(
                    anchorBounds = anchorBounds.roundToIntRect(),
                    windowSize = windowSize,
                    layoutDirection = layoutDirection,
                    popupContentSize = contentSize,
                    anchorTappedPosition = state.triggerPosition.toIntOffset()
                )
                state.pickerPlacement = calculatePickerPlacement(offset, anchorBounds)
                offset
            }
            .onGloballyPositioned { childCoordinates ->
                // Update the picker bounds when the picker is placed
                val bounds = childCoordinates.boundsInRoot()
                state.pickerBounds = bounds
            }
    ) {

        val alignment = when (state.pickerPlacement) {
            Placement.ABOVE -> Alignment.BottomCenter
            Placement.BELOW -> Alignment.TopCenter
        }

        if (animatedVisibility) {
            val height by animateDpAsState(
                label = "Container height",
                targetValue = when {
                    state.hoveredReaction != null -> ReactionItemSizeOnAnyHover
                    else -> ReactionItemSize
                } + ReactionPickerPadding.calculateTopPadding() +
                        ReactionPickerPadding.calculateBottomPadding()
            )

            ReactionPickerContainer(
                modifier = Modifier
                    .align(alignment)
                    .fillMaxWidth()
                    .height(height),
                state = state,
                anchorBounds = anchorBounds,
                containerShape = containerShape,
                containerColor = containerColor,
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ReactionPickerPadding),
                horizontalArrangement = Arrangement.spacedBy(properties.spaceBetweenReactions),
                verticalAlignment = verticalAlignment
            ) {
                state.reactions.forEachIndexed { index, reaction ->
                    AnimatedReactionItem(
                        index = index,
                        reaction = reaction,
                        state = state,
                        label = label,
                        icon = icon,
                        properties = properties,
                        anchorBounds = anchorBounds,
                        delayMillis = Duration.VERY_VERY_SHORT_DURATION * index,
                        onShown = {
                            // Perform haptic feedback
                            if (properties.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            // Enable pointer tracking when the last item is shown
                            if (index == state.reactions.lastIndex) {
                                state.enablePointerTracking()
                            }
                        },
                        onDismiss = {
                            //If the picker is dismissed with a selected reaction
                            //wait until the selected reaction is dismissed
                            if (state.isDismissingWithSelection) {
                                if (reaction === state.selectedReaction) {
                                    animatedVisibility = false
                                }
                                return@AnimatedReactionItem
                            }

                            // Hide the picker when the last item is dismissed
                            if (index == state.reactions.lastIndex) {
                                animatedVisibility = false
                            }
                        },
                    )
                }
            }
        }
    }

    BackHandler(enabled = properties.dismissOnBackPress) {
        state.dismiss()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedReactionItem(
    index: Int,
    reaction: Reaction,
    state: ReactionPickerState,
    properties: ReactionPickerProperties,
    anchorBounds: Rect,
    onShown: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = Duration.VERY_SHORT_DURATION,
    label: @Composable (String) -> Unit = {},
    icon: @Composable (Reaction) -> Unit = {},
) {

    val density = LocalDensity.current
    val alpha = remember { Animatable(0f) }
    val yOffset = remember { Animatable(anchorBounds.height) }
    val xOffset = remember { Animatable(0f) }

    var itemBounds by remember { mutableStateOf(Rect.Zero) }

    val currentOnShown by rememberUpdatedState(onShown)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val tooltipState = rememberTooltipState(isPersistent = true)

    // Calculate the offset multiplier based on the picker placement
    val offsetMultiplier = when (state.pickerPlacement) {
        Placement.ABOVE -> 1
        Placement.BELOW -> -1
    }

    // Start animation when the picker is about to be shown
    LaunchedEffect(Unit) {
        // Concurrently animate the alpha and translation
        listOf(
            async {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis, delayMillis)
                )
            },
            async {
                yOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis, delayMillis)
                )
            }
        ).awaitAll()

        currentOnShown()
    }

    // Start animation when the picker is about to be dismissed
    LaunchedEffect(state.isVisible) {
        if (state.isVisible) return@LaunchedEffect
        tooltipState.dismiss()

        // Concurrently animate the alpha and translation
        if (state.isDismissingWithSelection && state.selectedReaction === reaction) {
            listOf(
                async {
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(Duration.SHORT_DURATION)
                    )
                },
                async {
                    xOffset.animateTo(
                        targetValue = anchorBounds.left - itemBounds.left,
                        animationSpec = tween(Duration.SHORT_DURATION)
                    )
                }
            ).awaitAll()
        } else {
            listOf(
                async {
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis)
                    )
                },
                async {
                    yOffset.animateTo(
                        targetValue = anchorBounds.height,
                        animationSpec = tween(durationMillis)
                    )
                }
            ).awaitAll()
        }

        currentOnDismiss()
    }

    val iconSize by animateDpAsState(
        label = "Reaction size",
        targetValue = when {
            state.hoveredReaction === reaction -> ReactionItemSizeOnHover
            state.hoveredReaction != null -> ReactionItemSizeOnAnyHover
            else -> ReactionItemSize
        },
    )

    LaunchedEffect(iconSize) {
        //When icon size is about to be changed to ReactionItemSizeOnHover, show the tooltip
        if (iconSize > ReactionItemSizeOnHover - 8.dp) {
            tooltipState.show()
        } else {
            tooltipState.dismiss()
        }
    }

    val isSelectedReaction = state.selectedReaction === reaction

    TooltipBox(
        state = tooltipState,
        focusable = false,
        enableUserInput = false,
        positionProvider = rememberTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = properties.spaceBetweenReactionAndLabel,
            placement = state.pickerPlacement
        ),
        tooltip = {
            val labelText = when {
                reaction.labelRes != null -> stringResource(id = reaction.labelRes)
                reaction.label != null -> reaction.label
                else -> throw IllegalStateException("Either label or labelRes should be provided")
            }
            label(labelText)
        },
    ) {
        Box(
            modifier = modifier
                .size(iconSize)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .graphicsLayer {
                    this.alpha = alpha.value
                    this.translationX = xOffset.value

                    this.translationY = when {
                        isSelectedReaction && state.isDismissingWithSelection -> {
                            bezierAnimation(1 - alpha.value) * offsetMultiplier
                        }

                        else -> {
                            yOffset.value
                        }
                    }
                }
                .onGloballyPositioned { coordinates ->
                    // Inflate the bounds to make the reaction easier to tap
                    val inflateFactor = with(density) { 2.dp.toPx() }
                    val bounds = coordinates
                        .boundsInRoot()
                        .inflate(inflateFactor)

                    itemBounds = bounds
                    state.updateReactionItemBounds(index, bounds)
                },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                icon(reaction)
            }
        }
    }

}

@Composable
private fun rememberTooltipPositionProvider(
    spacingBetweenTooltipAndAnchor: Dp,
    placement: Placement,
): PopupPositionProvider {
    val tooltipAnchorSpacing = with(LocalDensity.current) {
        spacingBetweenTooltipAndAnchor.roundToPx()
    }
    return remember(tooltipAnchorSpacing, placement) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2

                val y = when (placement) {
                    Placement.ABOVE -> {
                        // Tooltip prefers to be above the anchor,
                        // but if this causes the tooltip to overlap with the anchor
                        // then we place it below the anchor
                        val posY = anchorBounds.top - popupContentSize.height - tooltipAnchorSpacing
                        if (posY < 0) anchorBounds.bottom + tooltipAnchorSpacing else posY
                    }

                    Placement.BELOW -> anchorBounds.bottom + tooltipAnchorSpacing
                }

                return IntOffset(x, y)
            }
        }
    }
}

@Composable
private fun ReactionPickerContainer(
    state: ReactionPickerState,
    anchorBounds: Rect,
    modifier: Modifier = Modifier,
    containerShape: Shape = CircleShape,
    containerColor: Color = Color.Black,
) {
    val containerAlpha = remember { Animatable(0f) }
    val containerYTranslate = remember { Animatable(anchorBounds.height) }

    // Start animation when the picker is about to be shown
    LaunchedEffect(Unit) {
        // Concurrently animate the alpha and translation
        listOf(
            async {
                containerAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = Duration.VERY_SHORT_DURATION
                    )
                )
            },
            async {
                containerYTranslate.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = Duration.VERY_SHORT_DURATION
                    )
                )
            }
        ).awaitAll()
    }

    // Start animation when the picker is about to be dismissed
    LaunchedEffect(state.isVisible) {
        if (state.isVisible) return@LaunchedEffect

        // Concurrently animate the alpha and translation
        listOf(
            async {
                containerAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = Duration.VERY_SHORT_DURATION)
                )
            },
            async {
                containerYTranslate.animateTo(
                    targetValue = anchorBounds.height,
                    animationSpec = tween(durationMillis = Duration.VERY_SHORT_DURATION)
                )
            }
        ).awaitAll()
    }

    Box(
        modifier = modifier
            .clip(containerShape)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .graphicsLayer {
                translationY = containerYTranslate.value
                alpha = containerAlpha.value
            }
            .background(containerColor),
    )
}

@Composable
fun ReactionIcon(
    reaction: Reaction,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
) {

    when {
        reaction.painterResource != null -> {
            Image(
                modifier = modifier,
                painter = painterResource(id = reaction.painterResource),
                contentDescription = null,
                colorFilter = colorFilter
            )
        }

        reaction.imageVector != null -> {
            Image(
                modifier = modifier,
                imageVector = reaction.imageVector,
                contentDescription = null,
                colorFilter = colorFilter
            )
        }
    }
}

/**
 * Modifier that adds the reaction picker anchor behavior to the composable. This modifier is not
 * scope safe and should be used only on descendants of [ReactionPickerLayout], otherwise an
 * [IllegalStateException] will be thrown.
 *
 * @param state The state of the reaction picker.
 *
 * @author Amsavarthan Lv
 */
fun Modifier.reactionPickerAnchor(
    state: ReactionPickerState,
): Modifier = composed {
    val stateHolder = LocalReactionPickerStateHolder.current
    return@composed this
        .onGloballyPositioned { childCoordinates ->
            // update the anchor bounds when the child is placed
            val bounds = childCoordinates.boundsInRoot()
            state.anchorBounds = bounds
        }
        .detectLongPress(state) { pointerPosition ->
            state.show(pointerPosition)
            stateHolder.updatePickerState(state)
        }
}

private fun Modifier.handleGesture(
    state: ReactionPickerState?,
): Modifier = this
    .pointerInput(state) {
        if (state == null) return@pointerInput
        awaitEachGesture {
            val singleTapTimeMillis = 20L
            val pass = PointerEventPass.Initial

            // wait for the first down press
            val pointer = awaitFirstDown(pass = pass)
            val pointerPosition = pointer.position

            if (state.isVisible) {
                pointer.consume()
                if (pointerPosition !in state.pickerBounds) {
                    state.dismiss()
                    return@awaitEachGesture
                }
            }

            try {
                withTimeout(singleTapTimeMillis) {
                    waitForUpOrCancellation(pass = pass)
                }.let { releasedEvent ->
                    if (!state.isVisible) return@awaitEachGesture

                    // check if the pointer is released
                    val isPointerUp = releasedEvent != null
                    if (isPointerUp) {
                        // get the reaction under the pointer
                        val reaction = state.getReaction(pointerPosition)
                        state.dismiss(reaction)
                        return@awaitEachGesture
                    }

                    // track the pointer position when the release event is cancelled
                    state.trackPointer(pointerPosition)
                }
            } catch (_: PointerEventTimeoutCancellationException) {
                // track the pointer position when timed out
                state.trackPointer(pointerPosition)
            }
        }
    }
    .pointerInput(state) {
        if (state == null || !state.isVisible) return@pointerInput
        val pass = PointerEventPass.Initial
        coroutineScope {
            awaitEachGesture {
                while (isActive) {
                    // wait for the next pointer event
                    val event = awaitPointerEvent(pass)

                    // consume the all the events
                    val changes = event.changes
                    changes.fastForEach { it.consume() }

                    val inputType = event.type

                    when (inputType) {
                        PointerEventType.Move -> {
                            // track the pointer position
                            state.trackPointer(event.changes[0].position)
                        }

                        PointerEventType.Release -> {
                            // dismiss the picker if the pointer is released
                            // on a reaction item
                            state.hoveredReaction?.let(state::dismiss)
                        }
                    }
                }
            }
        }
    }

private fun Modifier.detectLongPress(
    key: Any,
    onLongClick: (Offset) -> Unit,
): Modifier = this
    .pointerInput(key) {
        awaitEachGesture {
            val longPressTimeout = viewConfiguration.longPressTimeoutMillis
            val pass = PointerEventPass.Initial

            // wait for the first down press
            val pointer = awaitFirstDown(pass = pass)
            val inputType = pointer.type

            if (inputType == PointerType.Touch || inputType == PointerType.Stylus) {
                try {
                    // listen to if there is up gesture
                    // within the longPressTimeout limit
                    withTimeout(longPressTimeout) {
                        waitForUpOrCancellation(pass = pass)
                    }
                } catch (_: PointerEventTimeoutCancellationException) {
                    // handle long press - Show the reaction picker
                    onLongClick(pointer.position)

                    // consume the children's click handling
                    val changes = awaitPointerEvent(pass = pass).changes
                    changes.fastForEach { it.consume() }
                }
            }
        }
    }

private fun calculatePickerPlacement(
    pickerPosition: IntOffset,
    anchorBounds: Rect,
): Placement {
    return if (pickerPosition.y > anchorBounds.bottom) {
        Placement.BELOW
    } else {
        Placement.ABOVE
    }
}

private fun Offset.toIntOffset(): IntOffset {
    if (this == Offset.Unspecified) return IntOffset.Zero
    return IntOffset(x.toInt(), y.toInt())
}