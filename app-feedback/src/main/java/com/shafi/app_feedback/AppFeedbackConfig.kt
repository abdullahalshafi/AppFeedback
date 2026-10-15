package com.shafi.app_feedback

import androidx.annotation.ColorInt
import org.json.JSONObject

/**
 * Created by Shafi on 14/10/2026.
 *
 * App-wide settings, given once to [AppFeedback.init].
 *
 * @param baseUrl root of the feedback api, e.g. `https://example.com/feedback/api/`. It lives in the host app,
 * so the public library repo never shows the server.
 * @param apiKey the app's secret key, sent as `X-API-KEY`. The server files feedback under the app it belongs to.
 * @param userJson returns the logged-in user, with `user_id` and `user_name`. It runs on every check/show rather
 * than once here, because the user is only known after login and can change. Any extra keys are posted as they are.
 * @param title heading of the dialog.
 * @param subtitle line under the heading.
 * @param label label of the feedback field.
 * @param forceAfterDays days after the last feedback when the dialog opens by itself and can no longer be closed.
 * @param primaryColor overrides the colour taken from the app theme's `colorPrimary`.
 */
data class AppFeedbackConfig @JvmOverloads constructor(
    val baseUrl: String,
    val apiKey: String,
    val userJson: () -> JSONObject,
    val title: String = "We'd love your feedback",
    val subtitle: String = "Share your thoughts, ideas or problems.",
    val label: String = "Feedback",
    val forceAfterDays: Int = 14,
    @ColorInt val primaryColor: Int? = null,
)
