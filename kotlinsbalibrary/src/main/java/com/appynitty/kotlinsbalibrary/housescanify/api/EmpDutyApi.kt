package com.appynitty.kotlinsbalibrary.housescanify.api

import com.appynitty.kotlinsbalibrary.common.model.response.AttendanceResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchInRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchOutRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface EmpDutyApi {

    @POST("api/Save/QrEmployeeAttendenceIn")
    suspend fun saveEmpInPunchDetails(
        @Header("appId") appId: String?,
        @Header("Content-Type") content_type: String,
        @Body empPunchInRequest: EmpPunchInRequest
    ): Response<AttendanceResponse>

    @POST("api/Save/QrEmployeeAttendenceOut")
    suspend fun saveEmpOutPunchDetails(
        @Header("appId") appId: String?,
        @Header("Content-Type") content_type: String,
        @Header("Trail_id") trailId: String,
        @Body empPunchOutRequest: EmpPunchOutRequest
    ): Response<AttendanceResponse>

}