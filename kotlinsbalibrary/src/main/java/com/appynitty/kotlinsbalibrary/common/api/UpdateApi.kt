package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.ForceUpdateResponse
import com.appynitty.kotlinsbalibrary.common.model.response.GisLocResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UpdateApi {

    @GET("api/Get/VersionUpdate")
    suspend fun getVersionUpdate(
        @Header("appId") appId: String,
        @Header("version") version: Int
    ): Response<ForceUpdateResponse>


}