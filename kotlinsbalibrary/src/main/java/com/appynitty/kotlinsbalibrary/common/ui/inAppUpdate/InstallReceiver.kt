package com.appynitty.kotlinsbalibrary.common.ui.inAppUpdate

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.InstrumentationInfo
import android.content.pm.PackageInstaller
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.widget.Toast
import androidx.core.content.IntentCompat
import kotlin.system.exitProcess


private const val TAG = "AppInstaller"

class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val activityIntent =
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

                context.startActivity(activityIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                //  context.startActivity(activityIntent?.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT))

            }

            PackageInstaller.STATUS_SUCCESS -> {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    .startTone(ToneGenerator.TONE_PROP_ACK)

                // Toast.makeText(context , "App Updated Successfully !! Please restart the app",Toast.LENGTH_SHORT).show()

                //  forceRunApp(context)
            }

            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                Log.e(TAG, "received $status and $msg")
            }
        }
    }

    private fun restartApp(context: Context) {
        val mainIntent =
            IntentCompat.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.applicationContext.startActivity(mainIntent)
        exitProcess(0)
    }

    private fun forceRunApp(context: Context) {
        try {
            val info: InstrumentationInfo =
                context.packageManager.queryInstrumentation(context.packageName, 0)[0]
            val component = ComponentName(context, Class.forName(info.name))
            context.startInstrumentation(component, null, null)
        } catch (e: Throwable) {
            Log.e(TAG, "received ${e.message}")
        }
    }
}
