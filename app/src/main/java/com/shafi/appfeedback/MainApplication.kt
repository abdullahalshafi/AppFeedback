package com.shafi.appfeedback

import android.app.Application
import com.orhanobut.hawk.Hawk
import com.shafi.app_feedback.AppFeedback
import com.shafi.app_feedback.AppFeedbackConfig
import org.json.JSONObject

/**
 * Created by Shafi on 14/10/2026.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //like the host apps; the library shares this Hawk instead of building its own
        Hawk.init(this).build()

        AppFeedback.init(
            AppFeedbackConfig(
                baseUrl = BuildConfig.FEEDBACK_BASE_URL,
                apiKey = BuildConfig.FEEDBACK_API_KEY,
                //a real app reads AppUtil.getUserModel() here
                userJson = {
                    JSONObject()
                        .put("user_id", "123")
                        .put("user_name", "Rahim")
                },
            )
        )
    }
}
