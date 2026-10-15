package com.shafi.app_feedback.compose_ui.feedback

internal sealed interface FeedbackAction {

    data class OnFormValueChange<T>(val fieldType: FeedbackFieldType, val data: T) : FeedbackAction

    data object OnSubmitClicked : FeedbackAction
}
