package com.appynitty.kotlinsbalibrary.common.api

import jakarta.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val newUrl = originalUrl.newBuilder()
            .scheme("https")
            .host(baseUrlProvider.getBaseUrlHost())
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
