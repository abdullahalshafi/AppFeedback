package com.shafi.app_feedback.compose_ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Shafi on 14/10/2026.
 *
 * Circle pops in on a spring, a halo ripples out, then the tick draws itself.
 */
@Composable
internal fun AnimatedCheckmark(
    color: Color,
    checkColor: Color,
    modifier: Modifier = Modifier
) {

    val circleScale = remember { Animatable(0f) }
    val halo = remember { Animatable(0f) }
    val tick = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { circleScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)) }
        launch { halo.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        delay(220)
        tick.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }

    val tickPath = remember { Path() }
    val tickSegment = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    Canvas(modifier = modifier.size(88.dp)) {
        val radius = size.minDimension / 2f * 0.82f

        drawCircle(
            color = color.copy(alpha = 0.28f * (1f - halo.value)),
            radius = radius * (1f + 0.35f * halo.value)
        )
        drawCircle(color = color, radius = radius * circleScale.value)

        tickPath.reset()
        tickPath.moveTo(size.width * 0.31f, size.height * 0.52f)
        tickPath.lineTo(size.width * 0.44f, size.height * 0.645f)
        tickPath.lineTo(size.width * 0.69f, size.height * 0.385f)
        pathMeasure.setPath(tickPath, false)
        tickSegment.reset()
        pathMeasure.getSegment(0f, pathMeasure.length * tick.value, tickSegment, true)
        drawPath(
            path = tickSegment,
            color = checkColor,
            style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
