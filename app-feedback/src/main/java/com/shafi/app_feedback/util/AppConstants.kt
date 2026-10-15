package com.shafi.app_feedback.util

internal object AppConstants {

    const val NETWORK_TIMEOUT_SECONDS = 30L

    const val TAG = "AppFeedback"

    const val DEFAULT_PRIMARY_COLOR = 0xFF4355B9.toInt()

    const val FEEDBACK_MAX_LENGTH = 1000

    //date format
    const val API_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    //hawk keys (prefixed: the store is shared with the host app)
    const val LAST_FEEDBACK_AT = "app_feedback_last_feedback_at_"

    //intent keys
    const val BASE_URL = "base_url"
    const val API_KEY = "api_key"
    const val USER_JSON = "user_json"
    const val USER_ID = "user_id"
    const val TITLE = "title"
    const val SUBTITLE = "subtitle"
    const val LABEL = "label"
    const val IS_FORCED = "is_forced"
    const val PRIMARY_COLOR = "primary_color"

    //saved state keys
    const val FEEDBACK_TEXT = "feedback_text"
}
