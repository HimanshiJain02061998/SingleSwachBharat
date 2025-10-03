package com.appynitty.kotlinsbalibrary.common.utils

import android.app.Activity
import android.os.Build
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.appynitty.kotlinsbalibrary.R

class BackBtnPressedUtil {

    companion object {

        fun handleBackBtnPressed(
            activity: Activity,
            appCompatActivity: AppCompatActivity,
            lifecycleOwner: LifecycleOwner
        ) {
            if (Build.VERSION.SDK_INT >= 33) {
                activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT
                ) {
                    exitOnBackPressed(
                        activity
                    )
                }
            } else {
                appCompatActivity.onBackPressedDispatcher.addCallback(
                    lifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            exitOnBackPressed(
                                activity
                            )
                        }
                    })
            }
        }

        fun exitOnBackPressed(activity: Activity) {
            activity.finish()
            activity.overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
    }
}