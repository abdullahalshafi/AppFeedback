package com.shafi.app_feedback.repository

import com.shafi.app_feedback.api.Resource
import com.shafi.app_feedback.util.NoConnectivityException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal abstract class BaseRepository {

    suspend fun <T> doApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }

                    is NoConnectivityException, is SocketTimeoutException, is ConnectException, is UnknownHostException -> {
                        Resource.Failure(true, null, null)
                    }

                    else -> {
                        Resource.Failure(false, null, null)
                    }
                }
            }
        }
    }
}
