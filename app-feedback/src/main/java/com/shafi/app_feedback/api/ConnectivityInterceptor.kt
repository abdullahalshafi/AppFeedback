package com.shafi.app_feedback.api

import android.content.Context
import com.shafi.app_feedback.util.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

internal class ConnectivityInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
}
