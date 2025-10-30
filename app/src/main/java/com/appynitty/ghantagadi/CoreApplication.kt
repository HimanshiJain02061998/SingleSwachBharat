package com.appynitty.ghantagadi

import com.appynitty.kotlinsbalibrary.common.MyApplication
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class CoreApplication : MyApplication() {

    override fun onCreate() {
        super.onCreate()

     //   APP_ID = "3098"
        VERSION_CODE = BuildConfig.VERSION_NAME.toString()
        PACKAGE_NAME = BuildConfig.APPLICATION_ID
    }
}