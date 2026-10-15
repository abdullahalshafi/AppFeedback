package com.shafi.app_feedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.shafi.app_feedback.api.ApiClient
import com.shafi.app_feedback.api.Resource
import com.shafi.app_feedback.compose_ui.feedback.FeedbackActivity
import com.shafi.app_feedback.repository.MainRepository
import com.shafi.app_feedback.util.AppConstants.API_DATE_FORMAT
import com.shafi.app_feedback.util.AppConstants.API_KEY
import com.shafi.app_feedback.util.AppConstants.BASE_URL
import com.shafi.app_feedback.util.AppConstants.IS_FORCED
import com.shafi.app_feedback.util.AppConstants.LABEL
import com.shafi.app_feedback.util.AppConstants.PRIMARY_COLOR
import com.shafi.app_feedback.util.AppConstants.SUBTITLE
import com.shafi.app_feedback.util.AppConstants.TAG
import com.shafi.app_feedback.util.AppConstants.TITLE
import com.shafi.app_feedback.util.AppConstants.USER_ID
import com.shafi.app_feedback.util.AppConstants.USER_JSON
import com.shafi.app_feedback.util.AppUtil
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

/**
 * Created by Shafi on 14/10/2026.
 */
object AppFeedback {

    @Volatile
    private var config: AppFeedbackConfig? = null

    /**
     * Call once in MainApplication.onCreate. A config with a bad url, empty key or negative days is logged and
     * ignored, so later calls do nothing instead of crashing the app.
     */
    @JvmStatic
    fun init(config: AppFeedbackConfig) {
        if (isValidConfig(config)) this.config = config
    }

    /**
     * Call on every launch (e.g. in MainActivity.onCreate). Opens a dialog that cannot be closed when the user
     * has never sent feedback or [AppFeedbackConfig.forceAfterDays] have passed since the last one.
     * The status API is called only while no feedback date is saved on the device.
     *
     * @param extra keys posted with this feedback only, e.g. `"screen":"invoice"`. They are merged into the user
     * from [AppFeedbackConfig.userJson] and win on a clash.
     */
    @JvmStatic
    @JvmOverloads
    fun check(activity: ComponentActivity, extra: JSONObject? = null) {

        val config = getConfig() ?: return
        val userJson = getUserJson(config, extra) ?: return
        val userId = getValidUserId(userJson) ?: return
        AppUtil.initHawk(activity)

        val lastFeedbackAt = AppUtil.getLastFeedbackAt(userId)
        if (lastFeedbackAt != null) {
            if (AppUtil.isFeedbackDue(lastFeedbackAt, config.forceAfterDays)) {
                open(activity, config, userJson, userId, isForced = true)
            }
            return
        }

        val repository = MainRepository(ApiClient.getApiInterface(activity, config.baseUrl))

        activity.lifecycleScope.launch {

            when (val result = repository.repoGetLastFeedback(config.apiKey, userId)) {

                is Resource.Success -> {
                    val response = result.response
                    val serverFeedbackAt = AppUtil.getMillisFromString(response.last_feedback_at, API_DATE_FORMAT)

                    val isForced = when {
                        !response.has_feedback -> true

                        serverFeedbackAt == null -> {
                            Log.w(TAG, "unreadable last_feedback_at: ${response.last_feedback_at}")
                            false
                        }

                        else -> {
                            AppUtil.saveLastFeedbackAt(userId, serverFeedbackAt)
                            AppUtil.isFeedbackDue(serverFeedbackAt, config.forceAfterDays)
                        }
                    }

                    //starting an activity while the app is in the background is blocked on Android 10+
                    if (isForced) activity.withStarted { open(activity, config, userJson, userId, isForced = true) }
                }

                is Resource.Failure -> {
                    //no date saved, so the next launch asks again
                    Log.w(TAG, "status check failed: ${result.errorCode}")
                }

                is Resource.DBFailure -> {
                    Log.w(TAG, "status check failed: ${result.message}")
                }
            }
        }
    }

    /**
     * Opens the dialog on demand, e.g. from a "Feedback" button. It can be closed unless feedback is overdue.
     *
     * @param extra keys posted with this feedback only, as in [check].
     */
    @JvmStatic
    @JvmOverloads
    fun show(context: Context, extra: JSONObject? = null) {

        val config = getConfig() ?: return
        val userJson = getUserJson(config, extra) ?: return
        val userId = getValidUserId(userJson) ?: return
        AppUtil.initHawk(context)

        val lastFeedbackAt = AppUtil.getLastFeedbackAt(userId)
        val isForced = lastFeedbackAt != null && AppUtil.isFeedbackDue(lastFeedbackAt, config.forceAfterDays)
        open(context, config, userJson, userId, isForced)
    }

    private fun open(
        context: Context,
        config: AppFeedbackConfig,
        userJson: JSONObject,
        userId: String,
        isForced: Boolean
    ) {

        if (FeedbackActivity.isShowing) return

        val intent = Intent(context, FeedbackActivity::class.java).apply {
            putExtra(BASE_URL, config.baseUrl)
            putExtra(API_KEY, config.apiKey)
            //an intent can't carry a JSONObject; the string is also a snapshot the caller can't change later
            putExtra(USER_JSON, userJson.toString())
            putExtra(USER_ID, userId)
            putExtra(TITLE, config.title)
            putExtra(SUBTITLE, config.subtitle)
            putExtra(LABEL, config.label)
            putExtra(IS_FORCED, isForced)
            config.primaryColor?.let { putExtra(PRIMARY_COLOR, it) }
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun getConfig(): AppFeedbackConfig? {
        return config ?: run {
            Log.e(TAG, "no valid config: call AppFeedback.init() in MainApplication.onCreate first")
            null
        }
    }

    //a feedback library must never crash the host app, so a bad config is logged and skipped
    private fun isValidConfig(config: AppFeedbackConfig): Boolean {

        if (config.baseUrl.toHttpUrlOrNull() == null) {
            Log.e(TAG, "baseUrl is not a valid http(s) url: ${config.baseUrl}")
            return false
        }

        if (config.apiKey.isBlank()) {
            Log.e(TAG, "apiKey is empty")
            return false
        }

        if (config.forceAfterDays < 0) {
            Log.e(TAG, "forceAfterDays must be 0 or more")
            return false
        }

        return true
    }

    //the host's function may throw (e.g. no user saved yet), which must not crash the app
    private fun getUserJson(config: AppFeedbackConfig, extra: JSONObject?): JSONObject? {
        return try {
            //a copy, so the host's objects are never changed
            JSONObject(config.userJson().toString()).apply {
                extra?.keys()?.forEach { key -> put(key, extra.get(key)) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "userJson failed: ${e.message}")
            null
        }
    }

    private fun getValidUserId(userJson: JSONObject): String? {

        val userId = userJson.optString("user_id")
        if (userId.isBlank() || userJson.optString("user_name").isBlank()) {
            Log.e(TAG, "userJson needs user_id and user_name: $userJson")
            return null
        }

        return userId
    }
}
