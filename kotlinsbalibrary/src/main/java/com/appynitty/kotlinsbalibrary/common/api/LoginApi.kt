package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import com.appynitty.kotlinsbalibrary.common.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by sanath gosavi
 */
interface LoginApi {

    @POST("api/Account/Login")
    suspend fun saveLoginDetails(
        @Header("appId") appId: String,
        @Header("Content-Type") content_type: String,
        @Body loginPojo: LoginRequest
    ): Response<LoginResponse>

}