package com.shafi.app_feedback.compose_ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Created by Shafi on 14/10/2026.
 *
 * Full-width button that dips on press and swaps its label for a spinner while loading.
 */
@Composable
internal fun LoadingButton(
    text: String,
    icon: ImageVector,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "press"
    )

    Button(
        //stays coloured while loading instead of turning grey; the click is just ignored
        onClick = { if (!isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        interactionSource = interactionSource
    ) {

        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(initialScale = 0.7f, animationSpec = tween(200)))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 0.7f, animationSpec = tween(120)))
            },
            contentAlignment = Alignment.Center,
            label = "loading"
        ) { loading ->

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
