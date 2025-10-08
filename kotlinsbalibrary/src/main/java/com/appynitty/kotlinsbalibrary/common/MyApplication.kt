package com.appynitty.kotlinsbalibrary.common

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration


open class MyApplication : Application(), ViewModelStoreOwner {

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        APP_ID = ""
    }

    companion object {
        lateinit var instance: MyApplication
            private set

        lateinit var APP_ID: String
        lateinit var VERSION_CODE : String
        lateinit var PACKAGE_NAME : String
    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }


    override val viewModelStore: ViewModelStore
        get() = appViewModelStore


}