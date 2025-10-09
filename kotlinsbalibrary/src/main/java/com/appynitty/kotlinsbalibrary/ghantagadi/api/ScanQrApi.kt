package com.appynitty.kotlinsbalibrary.ghantagadi.api


import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.GarbageCollectionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ScanQrApi {

    @POST("api/Save/GarbageCollectionOfflineUpload")
    suspend fun saveGarbageCollectionOfflineData(
        @Header("appId") appId: String,
        @Header("typeId") typeId: String,
        @Header("batteryStatus") batteryStatus: Int,
        @Header("Content-Type") contentType: String,
        @Header("imeino") imeino: String?,
        @Body garbageCollectionDataList: List<GarbageCollectionData>
    ): Response<List<GarbageCollectionResponse>>


    @POST("api/Save/GarbageCollectionOnlineUpload")
    suspend fun saveGarbageCollectionOnlineData(
        @Header("appId") appId: String,
        @Header("typeId") typeId: String,
        @Header("batteryStatus") batteryStatus: Int,
        @Header("Content-Type") contentType: String,
        @Header("imeino") imeino: String?,
        @Body garbageCollectionDataList: GarbageCollectionData
    ): Response<List<GarbageCollectionResponse>>
}
