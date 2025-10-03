package com.appynitty.kotlinsbalibrary.common.utils

import android.content.Context
import android.content.IntentSender.SendIntentException
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.appynitty.kotlinsbalibrary.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class TurnOnGps {

    companion object {


        fun gpsStatusCheck(
            ctx: Context?,
            resolutionForResult: ActivityResultLauncher<IntentSenderRequest>
        ) {

            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(3000)
                    .setMaxUpdateDelayMillis(5000)
                    .setMinUpdateDistanceMeters(5f)
                    .build()

            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest)
            val task = LocationServices.getSettingsClient(
                ctx!!
            ).checkLocationSettings(builder.build())
            task.addOnCompleteListener { task1: Task<LocationSettingsResponse?> ->
                try {

                    if (CommonUtils.isAirplaneModeOn(ctx)) {
                        CustomToast.showWarningToast(
                            ctx, ctx.resources.getString(R.string.turn_off_airplane_mode)
                        )
                    } else {
                        val response =
                            task1.getResult(ApiException::class.java)
                    }
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable = exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                val intentSenderRequest = IntentSenderRequest
                                    .Builder(exception.resolution).build()
                                resolutionForResult.launch(intentSenderRequest)

//                                resolvable.startResolutionForResult(
//                                    (ctx as Activity?)!!,
//                                    101
//                                )
                            } catch (e: SendIntentException) {
                                // Ignore the error.
                            } catch (e: ClassCastException) {
                                // Ignore, should be an impossible error.
                            }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                    }
                }
            }
        }
    }
}