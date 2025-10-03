package com.appynitty.kotlinsbalibrary.housescanify.repository

import com.appynitty.kotlinsbalibrary.housescanify.api.EmpGcApi
import com.appynitty.kotlinsbalibrary.housescanify.api.MasterPlateApi
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import javax.inject.Inject

class MasterPlateRepository @Inject constructor(
    private val empGcApi: EmpGcApi,
    private val masterPlateApi: MasterPlateApi
) {

    suspend fun saveMasterPlateCollectionOnline(
        appId: String,
        contentType: String,
        masterPlateCollectionData: EmpGarbageCollectionRequest
    ) = empGcApi.saveEmpGarbageCollectionOnlineData(
        appId,
        contentType,
        masterPlateCollectionData
    )

    suspend fun masterPlateExists(appId: String, referenceId: String) =
        masterPlateApi.masterPlateExists(appId, referenceId)

    suspend fun houseIdExists(appId: String, referenceId: String) =
        masterPlateApi.houseIdExists(appId, referenceId)

}