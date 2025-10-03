package com.appynitty.kotlinsbalibrary.housescanify.repository

import com.appynitty.kotlinsbalibrary.housescanify.api.EmpDutyApi
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchInRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchOutRequest
import javax.inject.Inject


class EmpDutyRepository @Inject constructor(
    private val empDutyApi: EmpDutyApi
) {
    suspend fun saveEmpPunchInDetails(
        appId: String,
        content_type: String,
        inPunchRequest: EmpPunchInRequest
    ) = empDutyApi.saveEmpInPunchDetails(appId, content_type, inPunchRequest)

    suspend fun saveEmpPunchOutDetails(
        appId: String,
        content_type: String,
        trailId: String,
        outPunchRequest: EmpPunchOutRequest
    ) = empDutyApi.saveEmpOutPunchDetails(appId, content_type, trailId , outPunchRequest)

}