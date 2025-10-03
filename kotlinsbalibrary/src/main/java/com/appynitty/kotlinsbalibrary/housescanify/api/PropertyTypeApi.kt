package com.appynitty.kotlinsbalibrary.housescanify.api

import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface PropertyTypeApi {

    @GET("api/Get/PropertyTypeList")
    suspend fun getPropertyList(
        @Header("AppId") appId: String
    ): Response<List<PropertyType>>

}