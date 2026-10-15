package com.shafi.app_feedback.compose_ui.feedback

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shafi.app_feedback.R
import com.shafi.app_feedback.compose_ui.components.AnimatedCheckmark
import com.shafi.app_feedback.compose_ui.components.ErrorBanner
import com.shafi.app_feedback.compose_ui.components.FeedbackBadge
import com.shafi.app_feedback.compose_ui.components.LoadingButton
import com.shafi.app_feedback.compose_ui.components.ShakeController
import com.shafi.app_feedback.compose_ui.components.rememberShakeController
import com.shafi.app_feedback.compose_ui.components.shake
import com.shafi.app_feedback.compose_ui.theme.AppFeedbackTheme
import com.shafi.app_feedback.compose_ui.theme.CloseBackground
import com.shafi.app_feedback.compose_ui.theme.FieldBackground
import com.shafi.app_feedback.compose_ui.theme.Scrim
import com.shafi.app_feedback.util.AppConstants.DEFAULT_PRIMARY_COLOR
import com.shafi.app_feedback.util.AppConstants.FEEDBACK_MAX_LENGTH
import com.shafi.app_feedback.util.AppConstants.IS_FORCED
import com.shafi.app_feedback.util.AppConstants.LABEL
import com.shafi.app_feedback.util.AppConstants.PRIMARY_COLOR
import com.shafi.app_feedback.util.AppConstants.SUBTITLE
import com.shafi.app_feedback.util.AppConstants.TITLE
import com.shafi.app_feedback.util.AppUtil
import com.shafi.app_feedback.util.getErrorMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Shafi on 14/10/2026.
 *
 * A see-through activity instead of a Dialog, so any host works (XML or Compose, with or without fragments),
 * the text survives rotation, and a required dialog comes back after the user leaves and reopens the app.
 */
class FeedbackActivity : ComponentActivity() {

    private val viewModel: FeedbackViewModel by viewModels { FeedbackViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        //light icons over the dark scrim
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        isShowing = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        }

        val title = intent.getStringExtra(TITLE).orEmpty()
        val subtitle = intent.getStringExtra(SUBTITLE).orEmpty()
        val label = intent.getStringExtra(LABEL) ?: "Feedback"
        val isForced = intent.getBooleanExtra(IS_FORCED, false)
        val primaryColor = if (intent.hasExtra(PRIMARY_COLOR)) {
            intent.getIntExtra(PRIMARY_COLOR, DEFAULT_PRIMARY_COLOR)
        } else {
            AppUtil.getHostPrimaryColor(this) ?: DEFAULT_PRIMARY_COLOR
        }

        setContent {
            AppFeedbackTheme(primary = Color(primaryColor)) {

                val state by viewModel.state.collectAsStateWithLifecycle()
                FeedbackScreenRoot(state, title, subtitle, label, isForced) { action ->
                    viewModel.onAction(action)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) isShowing = false
    }

    @Composable
    private fun FeedbackScreenRoot(
        state: FeedbackState,
        title: String,
        subtitle: String,
        label: String,
        isForced: Boolean,
        onAction: (FeedbackAction) -> Unit
    ) {

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current
        val cardShake = rememberShakeController()
        val fieldShake = rememberShakeController()

        //saved so a rotation does not replay the entrance
        var hasEntered by rememberSaveable { mutableStateOf(false) }
        val visibleState = remember { MutableTransitionState(hasEntered) }
        var isClosing by remember { mutableStateOf(false) }

        val canDismiss = (!isForced || state.submitSuccessMessage != null) && !state.isSubmitApiLoading
        val close: () -> Unit = {
            if (!isClosing) {
                isClosing = true
                visibleState.targetState = false
            }
        }
        val tryClose: () -> Unit = {
            if (canDismiss) {
                close()
            } else if (!state.isSubmitApiLoading) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch { cardShake.shake() }
            }
        }

        //errorBody is a one-shot stream, so it is read once per failure
        val apiFailureMessage = remember(state.apiFailure?.id) {
            state.apiFailure?.let { context.getErrorMessage(it.apiFailure) }
        }

        LaunchedEffect(Unit) {
            visibleState.targetState = true
            hasEntered = true
        }

        when {

            state.submitSuccessMessage != null -> {
                LaunchedEffect(state.submitSuccessMessage.id) {
                    delay(1800)
                    close()
                }
            }

            state.validationErrorMessage != null -> {
                LaunchedEffect(state.validationErrorMessage.id) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    fieldShake.shake()
                }
            }
        }

        //the card has already animated out in Compose
        if (isClosing && visibleState.isIdle && !visibleState.currentState) {
            LaunchedEffect(Unit) {
                finish()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                }
            }
        }

        BackHandler(onBack = tryClose)

        val scrimAlpha by animateFloatAsState(
            targetValue = if (visibleState.targetState) 0.55f else 0f,
            animationSpec = tween(if (visibleState.targetState) 260 else 200),
            label = "scrim"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = tryClose
                )
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(220)) +
                        scaleIn(initialScale = 0.86f, animationSpec = spring(dampingRatio = 0.68f, stiffness = 380f)) +
                        slideInVertically(spring(dampingRatio = 0.8f, stiffness = 380f)) { it / 8 },
                exit = fadeOut(tween(180)) +
                        scaleOut(targetScale = 0.94f, animationSpec = tween(200)) +
                        slideOutVertically(tween(200)) { it / 14 },
                label = "card"
            ) {
                FeedbackCard(
                    state = state,
                    title = title,
                    subtitle = subtitle,
                    label = label,
                    isForced = isForced,
                    errorMessage = state.apiErrorMessage?.message ?: apiFailureMessage,
                    modifier = Modifier.shake(cardShake),
                    fieldShake = fieldShake,
                    onAction = onAction,
                    onClose = tryClose
                )
            }
        }
    }

    companion object {
        @Volatile
        internal var isShowing = false
            private set
    }
}

@Composable
private fun FeedbackCard(
    state: FeedbackState,
    title: String,
    subtitle: String,
    label: String,
    isForced: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    fieldShake: ShakeController = rememberShakeController(),
    onAction: (FeedbackAction) -> Unit = {},
    onClose: () -> Unit = {}
) {

    //Surface swallows taps, so touching the card never reaches the scrim's close
    Surface(
        modifier = modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 18.dp
    ) {

        AnimatedContent(
            targetState = state.submitSuccessMessage != null,
            transitionSpec = {
                (fadeIn(tween(260, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(260, delayMillis = 90)))
                    .togetherWith(fadeOut(tween(90)))
                    .using(SizeTransform(clip = false))
            },
            label = "content"
        ) { isSubmitted ->

            if (isSubmitted) {
                SuccessContent()
            } else {
                FormContent(
                    state = state,
                    title = title,
                    subtitle = subtitle,
                    label = label,
                    isForced = isForced,
                    errorMessage = errorMessage,
                    fieldShake = fieldShake,
                    onAction = onAction,
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
private fun FormContent(
    state: FeedbackState,
    title: String,
    subtitle: String,
    label: String,
    isForced: Boolean,
    errorMessage: String?,
    fieldShake: ShakeController,
    onAction: (FeedbackAction) -> Unit,
    onClose: () -> Unit
) {

    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    //a focused field reopens the keyboard when readOnly turns off again after the submit
    LaunchedEffect(state.isSubmitApiLoading) {
        if (state.isSubmitApiLoading) focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Row(verticalAlignment = Alignment.Top) {

            FeedbackBadge(color = colors.primary, dotColor = colors.onPrimary)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            if (!isForced) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onClose,
                    enabled = !state.isSubmitApiLoading,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CloseBackground)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.app_feedback_close),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (isForced) {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.app_feedback_please_share_your_feedback_to_continue),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val feedback = state.feedbackInfo.feedback
        OutlinedTextField(
            value = feedback.value,
            onValueChange = { onAction(FeedbackAction.OnFormValueChange(FeedbackFieldType.FEEDBACK, it)) },
            modifier = Modifier
                .fillMaxWidth()
                .shake(fieldShake),
            readOnly = state.isSubmitApiLoading,
            label = { Text(label) },
            placeholder = {
                Text(
                    text = stringResource(R.string.app_feedback_feedback_points),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            isError = feedback.error != null,
            supportingText = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = feedback.error.orEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "${feedback.value.length}/$FEEDBACK_MAX_LENGTH")
                }
            },
            minLines = 5,
            maxLines = 8,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = FieldBackground,
                errorContainerColor = Color.White,
                unfocusedBorderColor = colors.outline,
                unfocusedLabelColor = colors.onSurfaceVariant
            )
        )

        ErrorBanner(message = errorMessage)

        Spacer(modifier = Modifier.height(20.dp))

        LoadingButton(
            text = stringResource(R.string.app_feedback_submit),
            icon = Icons.AutoMirrored.Filled.Send,
            isLoading = state.isSubmitApiLoading,
            onClick = { onAction(FeedbackAction.OnSubmitClicked) }
        )
    }
}

@Composable
private fun SuccessContent() {

    val textState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { textState.targetState = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AnimatedCheckmark(
            color = MaterialTheme.colorScheme.primary,
            checkColor = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(18.dp))

        AnimatedVisibility(
            visibleState = textState,
            enter = fadeIn(tween(320, delayMillis = 380)) +
                    slideInVertically(tween(320, delayMillis = 380)) { it / 2 },
            label = "thanks"
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_feedback_thank_you),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.app_feedback_your_feedback_has_been_submitted),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF8A8D96)
@Composable
private fun FeedbackCardPreview() {
    AppFeedbackTheme(primary = Color(0xFF0E7C66)) {
        Box(modifier = Modifier.padding(20.dp)) {
            FeedbackCard(
                state = FeedbackState(),
                title = "We'd love your feedback",
                subtitle = "Share your thoughts, ideas or problems. It helps us make the app better.",
                label = "Feedback",
                isForced = false,
                errorMessage = null
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF8A8D96)
@Composable
private fun FeedbackCardForcedPreview() {
    AppFeedbackTheme(primary = Color(DEFAULT_PRIMARY_COLOR)) {
        Box(modifier = Modifier.padding(20.dp)) {
            FeedbackCard(
                state = FeedbackState(),
                title = "We'd love your feedback",
                subtitle = "Share your thoughts, ideas or problems. It helps us make the app better.",
                label = "Feedback",
                isForced = true,
                errorMessage = "No internet! Please check your connection."
            )
        }
    }
}
