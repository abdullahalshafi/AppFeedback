package com.shafi.app_feedback.api

import okhttp3.ResponseBody

internal sealed class Resource<out T> {

    data class Success<out T>(val response: T) : Resource<T>()

    data class Failure(
        val isNetWorkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()

    data class DBFailure(
        val isDBError: Boolean,
        val message: String?
    ) : Resource<Nothing>()
}
