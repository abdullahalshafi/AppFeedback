package com.shafi.app_feedback.compose_ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

//light only, like the host apps
@Composable
internal fun AppFeedbackTheme(
    primary: Color,
    content: @Composable () -> Unit
) {

    val colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = if (primary.luminance() > 0.5f) TextPrimary else Color.White,
        primaryContainer = primary.copy(alpha = 0.10f).compositeOver(Color.White),
        onPrimaryContainer = primary,
        background = Color.White,
        onBackground = TextPrimary,
        surface = Color.White,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary,
        surfaceTint = Color.White,
        outline = Outline,
        error = Red,
        errorContainer = RedLight,
        onErrorContainer = RedDark
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
