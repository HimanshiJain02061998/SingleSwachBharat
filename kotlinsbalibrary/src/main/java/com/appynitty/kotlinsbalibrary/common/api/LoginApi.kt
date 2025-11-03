package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import com.appynitty.kotlinsbalibrary.common.model.response.AppAssetsResponse
import com.appynitty.kotlinsbalibrary.common.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

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

    @GET
    suspend fun getAppAssetsByAppId(
        @Url url: String = "https://ictcoreapi.ictsbm.com/api/GetAppAssetsByAppId",
        @Header("appid") appId: String
    ): Response<AppAssetsResponse>
}