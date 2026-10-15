package com.shafi.app_feedback.compose_ui.feedback

import com.shafi.app_feedback.util.FormFieldType

internal enum class FeedbackFieldType {
    FEEDBACK
}

internal data class FeedbackInfo(

    val feedback: FormFieldType<String> = FormFieldType("")
)
