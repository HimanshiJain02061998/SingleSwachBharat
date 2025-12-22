package com.appynitty.kotlinsbalibrary.common.api

import android.net.Uri
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import jakarta.inject.Inject
import jakarta.inject.Singleton
import androidx.core.net.toUri

@Singleton
class BaseUrlProvider @Inject constructor() {
    fun getBaseUrlHost(): String {
        return CommonUtils.BASE_URL.toUri().host
            ?: throw IllegalArgumentException("Invalid base URL: $CommonUtils.BASE_URL")
    }
}
