package com.appynitty.kotlinsbalibrary.common.api

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenApi {

    @POST("api/Account/getToken")
    suspend fun generateToken(
        @Header("appId") tokenAppId: String,
        @Header("Content-Type") content_type: String
    ): Response<String>

}