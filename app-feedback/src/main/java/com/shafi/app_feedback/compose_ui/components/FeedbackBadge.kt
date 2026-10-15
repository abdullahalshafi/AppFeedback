package com.shafi.app_feedback.compose_ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Created by Shafi on 14/10/2026.
 *
 * Chat bubble whose three dots bounce like a typing indicator.
 */
@Composable
internal fun FeedbackBadge(
    color: Color,
    dotColor: Color,
    modifier: Modifier = Modifier
) {

    val transition = rememberInfiniteTransition(label = "typing")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "typing phase"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.size(26.dp)) {
            val w = size.width
            val h = size.height
            val top = h * 0.06f
            val bottom = h * 0.78f

            drawRoundRect(
                color = color,
                topLeft = Offset(0f, top),
                size = Size(w, bottom - top),
                cornerRadius = CornerRadius(w * 0.3f)
            )
            val tail = Path().apply {
                moveTo(w * 0.20f, bottom - 1f)
                lineTo(w * 0.16f, h)
                lineTo(w * 0.46f, bottom - 1f)
                close()
            }
            drawPath(tail, color)

            val centerY = (top + bottom) / 2f
            for (i in 0..2) {
                val local = (phase - i * 0.14f + 1f) % 1f
                val lift = if (local < 0.36f) sin(local / 0.36f * PI).toFloat() else 0f
                drawCircle(
                    color = dotColor,
                    radius = w * 0.075f,
                    center = Offset(w * (0.28f + i * 0.22f), centerY - lift * h * 0.09f)
                )
            }
        }
    }
}
