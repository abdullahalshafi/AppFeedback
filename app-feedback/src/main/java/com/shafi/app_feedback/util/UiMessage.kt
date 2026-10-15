package com.shafi.app_feedback.util

//one-shot message in a state data class; the id makes LaunchedEffect(id) fire again for a repeated text
internal data class UiMessage(
    val message: String,
    val id: Long = System.currentTimeMillis()
)
