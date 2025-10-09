package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.ScanQrApi
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import javax.inject.Inject


class GarbageCollectionRepo @Inject constructor(private val scanQrApi: ScanQrApi) {

    suspend fun saveGarbageCollectionOfflineData(
        appId: String,
        typeId: String,
        batteryStatus: Int,
        contentType: String,
        imeino: String?,
        garbageCollectionDataList: List<GarbageCollectionData>
    ) = scanQrApi.saveGarbageCollectionOfflineData(
        appId,
        typeId,
        batteryStatus,
        contentType,
        imeino,
        garbageCollectionDataList
    )

    suspend fun saveGarbageCollectionOnlineData(
        appId: String,
        typeId: String,
        batteryStatus: Int,
        contentType: String,
        imeino: String?,
        garbageCollectionDataList: GarbageCollectionData
    ) = scanQrApi.saveGarbageCollectionOnlineData(
        appId,
        typeId,
        batteryStatus,
        contentType,
        imeino,
        garbageCollectionDataList
    )
}