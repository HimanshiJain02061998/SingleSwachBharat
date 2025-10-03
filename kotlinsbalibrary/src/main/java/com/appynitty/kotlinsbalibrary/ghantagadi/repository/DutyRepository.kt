package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.DutyApi
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.OutPunchRequest
import javax.inject.Inject


class DutyRepository @Inject constructor(
    private val dutyApi: DutyApi
) {

    suspend fun getVehicleTypeDetails(
        appId: String,
        content_type: String
    ) = dutyApi.getVehicleTypeDetails(appId, content_type)


    suspend fun getVehicleNumberList(
        appId: String,
        content_type: String,
        vehicleTypeId: String
    ) = dutyApi.getVehicleNumberList(appId, content_type, vehicleTypeId)

    suspend fun saveInPunchDetails(
        appId: String,
        content_type: String,
        batteryStatus: Int,
        inPunchRequest: InPunchRequest
    ) = dutyApi.saveInPunchDetails(appId, content_type, batteryStatus, inPunchRequest)

    suspend fun getVehicleQrDetails(
        appId: String,
        contentType: String,
        referenceId: String,
        empType: String,
        currentLat: String,
        currentLon: String
    ) = dutyApi.getVehicleQrDetails(
        appId,
        contentType,
        referenceId,
        empType,
        currentLat,
        currentLon
    )

    suspend fun saveOutPunchDetails(
        appId: String,
        content_type: String,
        batteryStatus: Int,
        trailId: String,
        outPunchRequest: OutPunchRequest
    ) = dutyApi.saveOutPunchDetails(appId, content_type, batteryStatus, trailId, outPunchRequest)

    suspend fun getDumpYardIds(
        appId: String
    ) = dutyApi.getDumpYardIds(appId)
}