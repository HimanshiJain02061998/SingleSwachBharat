package com.appynitty.kotlinsbalibrary.housescanify.repository

import com.appynitty.kotlinsbalibrary.housescanify.api.EmpGcApi
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import javax.inject.Inject


class EmpGcRepository @Inject constructor(private val empGcApi: EmpGcApi){

    suspend fun saveGarbageCollectionOnlineData(
        appId: String,
        contentType: String,
        garbageCollectionData: EmpGarbageCollectionRequest
    ) = empGcApi.saveEmpGarbageCollectionOnlineData(
        appId,
        contentType,
        garbageCollectionData
    )

    suspend fun saveGarbageCollectionOfflineData(
        appId: String,
        contentType: String,
        garbageCollectionDataList: List<EmpGarbageCollectionRequest>
    ) = empGcApi.saveGarbageCollectionOfflineData(
        appId,
        contentType,
        garbageCollectionDataList
    )

    suspend fun getHouseOnMapHistory(
        appId: String,
        userId: String,
        date: String
    ) = empGcApi.getHouseOnMapHistory(
        appId,
        userId,
        date
    )

}