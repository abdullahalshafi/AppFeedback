package com.shafi.app_feedback.util

import com.shafi.app_feedback.api.Resource

internal data class ApiFailure(
    val apiFailure: Resource.Failure,
    val id: Long = System.currentTimeMillis()
)
