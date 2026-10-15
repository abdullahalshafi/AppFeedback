package com.shafi.app_feedback.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

internal object NetworkUtil {

    fun isNetworkAvailable(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //the library supports minSdk 21 hosts, where activeNetwork does not exist yet
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            return connectivityManager.activeNetworkInfo?.isConnected == true
        }

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            else -> false
        }
    }
}
