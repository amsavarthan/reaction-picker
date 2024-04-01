package dev.amsavarthan.reaction.picker.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.amsavarthan.reaction.picker.models.Reaction
import dev.amsavarthan.reaction.picker.reactionPickerAnchor
import dev.amsavarthan.reaction.picker.rememberReactionPickerState


@Composable
fun FeedItem(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            UserImage(modifier = Modifier.size(48.dp))
            UserDetails(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        FeedContent(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionButtons()
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
) {

    val items = listOf(
        Reaction(label = "Love", painterResource = R.drawable.love),
        Reaction(label = "Care", painterResource = R.drawable.care),
        Reaction(label = "Wow", painterResource = R.drawable.wow),
        Reaction(label = "Haha", painterResource = R.drawable.haha),
        Reaction(label = "Sad", painterResource = R.drawable.sad),
        Reaction(label = "Angry", painterResource = R.drawable.angry),
    )

    val state = rememberReactionPickerState(
        reactions = items,
        initialSelection = null,
    )

    IconButton(
        modifier = modifier.reactionPickerAnchor(state),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        onClick = state::toggleReaction
    ) {
        if (state.selectedReaction != null) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = state.selectedReaction!!.painterResource!!),
                contentDescription = "react to post",
            )
        } else {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.FavoriteBorder,
                contentDescription = "react to post",
            )
        }
    }
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun UserImage(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun UserDetails(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}