package com.shafi.app_feedback.repository

import com.google.gson.JsonObject
import com.shafi.app_feedback.api.ApiInterface
import com.shafi.app_feedback.api.Resource
import com.shafi.app_feedback.rest_model.GenericResponseModel
import com.shafi.app_feedback.rest_model.LastFeedbackResponseModel

/**
 * Created by Shafi on 14/10/2026.
 */
internal class MainRepository(private val apiInterface: ApiInterface) : BaseRepository() {

    suspend fun repoGetLastFeedback(apiKey: String, userId: String): Resource<LastFeedbackResponseModel> {
        return doApiCall { apiInterface.getLastFeedback(apiKey, userId) }
    }

    suspend fun repoSubmitFeedback(apiKey: String, feedbackUploadModel: JsonObject): Resource<GenericResponseModel> {
        return doApiCall { apiInterface.submitFeedback(apiKey, feedbackUploadModel) }
    }
}
