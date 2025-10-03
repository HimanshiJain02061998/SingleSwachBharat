package com.appynitty.kotlinsbalibrary.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

private const val TAG = "ConnectivityStatus"

class ConnectivityStatus(context: Context) : LiveData<Boolean>() {

    private var connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallbacks = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
            Log.i(TAG, "onAvailable: ")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
            Log.i(TAG, "onLost: ")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            postValue(false)
            Log.i(TAG, "onUnavailable: ")
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkInternet() {
        val network = connectivityManager.activeNetwork
        if (network == null) {
            postValue(false)
        }

        // checking internet capabilities

        val requestBuilder = NetworkRequest.Builder().apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // also for sdk version 23 or above
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        }.build()

        connectivityManager.registerNetworkCallback(requestBuilder, networkCallbacks)
    }

    override fun onActive() {
        super.onActive()
        checkInternet()
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallbacks)
    }
}