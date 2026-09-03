package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SoundWaveAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 3.dp
) {
    val transition = rememberInfiniteTransition(label = "sound_waves")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val h2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val h3 by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 50, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val h4 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bar1Height = if (isPlaying) maxHeight * h1 else maxHeight * 0.3f
        val bar2Height = if (isPlaying) maxHeight * h2 else maxHeight * 0.6f
        val bar3Height = if (isPlaying) maxHeight * h3 else maxHeight * 0.4f
        val bar4Height = if (isPlaying) maxHeight * h4 else maxHeight * 0.25f

        Box(
            modifier = Modifier
                .width(barWidth)
                .height(bar1Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(bar2Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(bar3Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(barWidth)
                .height(bar4Height)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}
