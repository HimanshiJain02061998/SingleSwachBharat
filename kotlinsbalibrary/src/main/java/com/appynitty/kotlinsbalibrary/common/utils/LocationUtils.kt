package com.appynitty.kotlinsbalibrary.common.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.appynitty.kotlinsbalibrary.common.location.GisLocationService

class LocationUtils {

    companion object {


        fun startGisLocationTracking(context: Context) {

            val intent = Intent(context, GisLocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(intent)
            } else {
                context.applicationContext.startService(intent)
            }

        }

        fun stopGisLocationTracking(context: Context) {
            context.applicationContext.stopService(Intent(context, GisLocationService::class.java))
        }


    }
}