package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.ModuleAccessResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ModuleAccessWebService {
    @GET("api/Get/ModuleAccess")
    suspend  fun getModuleAccess(
        @Header("AppId") appId: String,
        @Header("empType") empType: String?
    ): Response<ModuleAccessResponse>
}