package com.appynitty.kotlinsbalibrary.common

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import javax.inject.Inject



open class MyApplication : Application(), ViewModelStoreOwner {


    override fun onCreate() {
        super.onCreate()
        instance = this

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        APP_ID = ""

        var userDataStore= UserDataStore(this)

        CoroutineScope(Dispatchers.IO).launch {
            val tempAppId = userDataStore.getAppId.first()
            if (tempAppId != "") {
                APP_ID = tempAppId
            }
        }
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