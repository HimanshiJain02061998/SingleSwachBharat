package com.appynitty.kotlinsbalibrary.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

class AirplaneModeChangeReceiver : BroadcastReceiver() {

    val airplaneModeLiveData = MutableLiveData<Boolean>()
    // this function will be executed when the user changes his
    // airplane mode
    override fun onReceive(context: Context?, intent: Intent?) {

        // if getBooleanExtra contains null value,it will directly return back
        val isAirplaneModeEnabled = intent?.getBooleanExtra("state", false) ?: return

        // checking whether airplane mode is enabled or not
        if (isAirplaneModeEnabled) {

            airplaneModeLiveData.postValue(true)
        } else {

            airplaneModeLiveData.postValue(false)

        }
    }
}