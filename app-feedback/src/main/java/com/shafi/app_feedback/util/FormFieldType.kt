package com.shafi.app_feedback.util

//one form field in a Compose state: current value, picker options, validation error shown under the field
internal data class FormFieldType<T>(
    val value: T,
    val options: List<T> = emptyList(),
    val error: String? = null
)
