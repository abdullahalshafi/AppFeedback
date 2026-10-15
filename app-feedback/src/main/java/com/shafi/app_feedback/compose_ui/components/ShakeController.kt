package com.shafi.app_feedback.compose_ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Created by Shafi on 14/10/2026.
 *
 * Horizontal "no" shake: an empty submit shakes the field, closing a required dialog shakes the card.
 */
@Stable
internal class ShakeController {

    val offset = Animatable(0f)

    suspend fun shake() {
        offset.snapTo(0f)
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 460
                -14f at 50
                12f at 120
                -9f at 190
                7f at 260
                -4f at 330
                2f at 400
            }
        )
    }
}

@Composable
internal fun rememberShakeController() = remember { ShakeController() }

internal fun Modifier.shake(controller: ShakeController) = graphicsLayer {
    translationX = controller.offset.value * density
}
