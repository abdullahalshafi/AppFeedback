package com.shafi.app_feedback.api

import com.google.gson.JsonObject
import com.shafi.app_feedback.rest_model.GenericResponseModel
import com.shafi.app_feedback.rest_model.LastFeedbackResponseModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

internal interface ApiInterface {

    @GET("feedback/last")
    suspend fun getLastFeedback(
        @Header("X-API-KEY") apiKey: String,
        @Query("user_id") userId: String
    ): LastFeedbackResponseModel

    //body is the host app's user json plus "feedback", so its keys are not fixed
    @POST("feedback")
    suspend fun submitFeedback(
        @Header("X-API-KEY") apiKey: String,
        @Body feedbackUploadModel: JsonObject
    ): GenericResponseModel
}
