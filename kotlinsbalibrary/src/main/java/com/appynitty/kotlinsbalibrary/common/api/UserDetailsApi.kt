package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.UserDetailsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserDetailsApi {

    @GET("api/Get/User")
    suspend fun getUserDetails(
        @Header("appId") appId: String,
        @Header("Content-Type") content_type: String,
        @Header("userId") userId: String,
        @Header("typeId") typeId: String
    ): Response<UserDetailsResponse>

}