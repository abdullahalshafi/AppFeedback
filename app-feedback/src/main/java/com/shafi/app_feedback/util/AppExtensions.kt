package com.shafi.app_feedback.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.shafi.app_feedback.R
import com.shafi.app_feedback.api.Resource
import com.shafi.app_feedback.rest_model.ErrorModel

//same mapping as the apps' generateErrorMessage, but returns the text: the dialog shows it in the card
internal fun Context.getErrorMessage(t: Resource.Failure): String {
    return when (t.isNetWorkError) {

        true -> getString(R.string.app_feedback_no_internet_please_check_your_connection)

        false -> {
            when (t.errorCode) {

                //401 carries "Invalid or missing API key", 422 the field errors
                400, 401, 422 -> {
                    try {
                        val errorResponse: ErrorModel? = Gson().fromJson(
                            t.errorBody?.charStream(), ErrorModel::class.java
                        )
                        errorResponse?.message ?: getString(R.string.app_feedback_something_went_wrong)
                    } catch (e: JsonParseException) {
                        getString(R.string.app_feedback_bad_request)
                    }
                }

                404 -> getString(R.string.app_feedback_page_not_found)
                500 -> getString(R.string.app_feedback_internal_server_error)
                502 -> getString(R.string.app_feedback_bad_gateway)
                else -> getString(R.string.app_feedback_something_went_wrong)
            }
        }
    }
}
