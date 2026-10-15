package com.shafi.app_feedback.api

import android.content.Context
import com.shafi.app_feedback.BuildConfig
import com.shafi.app_feedback.util.AppConstants.NETWORK_TIMEOUT_SECONDS
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Shafi on 14/10/2026.
 *
 * Built by hand instead of a Hilt AppModule: a library module would force Hilt on every host (many have
 * none), and its OkHttpClient binding would clash with the host's own AppModule.
 */
internal object ApiClient {

    private var apiInterface: ApiInterface? = null
    private var apiBaseUrl = ""

    //the url comes from the host's config, so the cached client is rebuilt if it ever changes
    fun getApiInterface(context: Context, baseUrl: String): ApiInterface = synchronized(this) {
        apiInterface?.takeIf { apiBaseUrl == baseUrl }
            ?: provideApiInterface(provideOkHttpClient(context.applicationContext), baseUrl).also {
                apiInterface = it
                apiBaseUrl = baseUrl
            }
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return interceptor
    }

    private fun provideOkHttpClient(context: Context): OkHttpClient {

        return OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        //without it the server answers errors with an html page
                        .addHeader("Accept", "application/json")
                        .build()
                )
            }
            .addInterceptor(provideLoggingInterceptor())
            .addInterceptor(ConnectivityInterceptor(context))
            .build()
    }

    private fun provideApiInterface(okHttpClient: OkHttpClient, baseUrl: String): ApiInterface {
        return Retrofit.Builder()
            //retrofit throws on a base url without the trailing slash
            .baseUrl(baseUrl.removeSuffix("/") + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
