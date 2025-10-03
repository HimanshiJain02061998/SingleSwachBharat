package com.appynitty.kotlinsbalibrary.common.ui.inAppUpdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AppUpgradeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != "android.intent.action.MY_PACKAGE_REPLACED") {
            Toast.makeText(
                context,
                "App Updated Successfully !! Please restart the app",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}