package com.appynitty.kotlinsbalibrary.housescanify.api

import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpGcResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpHouseOnMapResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface EmpGcApi {

    @POST("api/Save/QrHPDCollections")
    suspend fun saveEmpGarbageCollectionOnlineData(
        @Header("appId") appId: String,
        @Header("Content-Type") contentType: String,
        @Body empGarbageCollectionRequest: EmpGarbageCollectionRequest
    ): Response<EmpGcResponse>

    @POST("api/Save/QrHPDCollectionsOffline")
    suspend fun saveGarbageCollectionOfflineData(
        @Header("appId") appId: String,
        @Header("Content-Type") contentType: String,
        @Body garbageCollectionDataList: List<EmpGarbageCollectionRequest>
    ): Response<List<EmpGcResponse>>

    @GET("api/Get/GetLatLongD")
    suspend fun getHouseOnMapHistory(
        @Header("appId") appId: String?,
        @Header("userId") userId: String?,
        @Header("fdate") date: String?
    ): Response<List<EmpHouseOnMapResponse>>



}