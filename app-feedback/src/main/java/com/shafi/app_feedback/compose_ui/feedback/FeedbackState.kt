package com.shafi.app_feedback.compose_ui.feedback

import com.shafi.app_feedback.util.ApiFailure
import com.shafi.app_feedback.util.UiMessage

internal data class FeedbackState(

    val isSubmitApiLoading: Boolean = false,
    val apiFailure: ApiFailure? = null,
    val apiErrorMessage: UiMessage? = null,
    val validationErrorMessage: UiMessage? = null,
    val submitSuccessMessage: UiMessage? = null,
    val feedbackInfo: FeedbackInfo = FeedbackInfo()
)
