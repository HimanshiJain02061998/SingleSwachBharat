package com.appynitty.kotlinsbalibrary.common.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitCurrentLocation(priority: Int): Location? {
    return suspendCancellableCoroutine {
        // to use for request cancellation upon coroutine cancellation
        val cts = CancellationTokenSource()
        getCurrentLocation(priority, cts.token)
            .addOnSuccessListener { location ->
                // remember location is nullable, this happens sometimes
                // when the request expires before an update is acquired
                if (location != null)
                    it.resume(location)
            }.addOnFailureListener { e ->
                it.resumeWithException(e)
            }

        it.invokeOnCancellation {
            cts.cancel()
        }
    }
}