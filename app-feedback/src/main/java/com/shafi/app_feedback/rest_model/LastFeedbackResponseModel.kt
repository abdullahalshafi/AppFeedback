package com.shafi.app_feedback.rest_model

/**
 * Created by Shafi on 14/10/2026.
 */
internal data class LastFeedbackResponseModel(
    val status: String?,
    val app_name: String?,
    val user_id: String?,
    val has_feedback: Boolean,
    val last_feedback_at: String?,
    val feedback_id: Int?,
    val feedback: String?
)
