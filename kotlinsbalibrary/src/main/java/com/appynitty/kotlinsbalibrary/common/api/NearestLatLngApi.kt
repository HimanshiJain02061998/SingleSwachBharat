package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.NearestLatLng
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface NearestLatLngApi {

    @GET("api/Get/NearestLatLong")
    suspend fun getNearestLatLongs(
        @Header("AppId") appId: String,
        @Header("sourceLat") sourceLat: String,
        @Header("sourceLong") sourceLong: String,
        @Header("userId") userId: Int
    ): Response<List<NearestLatLng>>

}