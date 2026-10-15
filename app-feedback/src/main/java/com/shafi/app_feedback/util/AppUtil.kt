package com.shafi.app_feedback.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.orhanobut.hawk.Hawk
import com.shafi.app_feedback.util.AppConstants.LAST_FEEDBACK_AT
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by Shafi on 14/10/2026.
 */
internal object AppUtil {

    //hawk
    fun initHawk(context: Context) {
        //host apps build Hawk in MainApplication; this covers the ones that don't
        if (!Hawk.isBuilt()) Hawk.init(context.applicationContext).build()
    }

    //keyed by user id so a second user on the same phone gets their own schedule
    fun saveLastFeedbackAt(userId: String, millis: Long) {
        Hawk.put(LAST_FEEDBACK_AT + userId, millis)
    }

    fun getLastFeedbackAt(userId: String): Long? {
        return Hawk.get<Long>(LAST_FEEDBACK_AT + userId)
    }

    fun isFeedbackDue(lastFeedbackAt: Long, forceAfterDays: Int): Boolean {
        return System.currentTimeMillis() - lastFeedbackAt >= TimeUnit.DAYS.toMillis(forceAfterDays.toLong())
    }

    //date
    fun getMillisFromString(givenDate: String?, givenFormat: String): Long? {
        if (givenDate.isNullOrEmpty()) return null
        return try {
            SimpleDateFormat(givenFormat, Locale.ENGLISH).parse(givenDate)?.time
        } catch (e: ParseException) {
            null
        }
    }

    //the host theme's colorPrimary, so the dialog matches each app without configuration
    fun getHostPrimaryColor(context: Context): Int? {
        val themeRes = context.applicationInfo.theme
        if (themeRes == 0) return null

        val theme = context.resources.newTheme().apply { applyStyle(themeRes, true) }
        val typedArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimary))
        val color = try {
            if (typedArray.hasValue(0)) typedArray.getColor(0, 0) else null
        } finally {
            typedArray.recycle()
        }
        //the plain platform theme leaves colorPrimary near white, useless on a white card
        return color?.takeIf { Color(it).alpha > 0.9f && Color(it).luminance() < 0.85f }
    }
}
