package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.request.GisLocRequest
import com.appynitty.kotlinsbalibrary.common.model.response.GisLocResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GisApi {

    @POST("api/Save/GarbageMapTrail")
    suspend fun saveGarbageMapGisLocations(
        @Header("AppId") appId: String,
        @Body gisLocRequest: GisLocRequest
    ): Response<GisLocResponse?>


    @POST("api/Save/HouseMapTrail")
    suspend fun saveHouseMapGisLocations(
        @Header("AppId") appId: String,
        @Body gisLocRequest: GisLocRequest
    ): Response<GisLocResponse?>


}