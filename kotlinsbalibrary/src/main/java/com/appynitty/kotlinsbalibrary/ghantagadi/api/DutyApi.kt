package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.common.model.response.AttendanceResponse
import com.appynitty.kotlinsbalibrary.common.model.response.VehicleQrDetailsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.OutPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DumpYardIds
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleNumberResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleTypeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DutyApi {

    /** Ghanta gadi */

    @GET("api/Get/VehicleType")
    suspend fun getVehicleTypeDetails(
        @Header("appId") appId: String,
        @Header("Content-Type") content_type: String
    ): Response<List<VehicleTypeResponse>>

    @GET("api/Get/Vehicles")
    suspend fun getVehicleNumberList(
        @Header("appId") appId: String,
        @Header("Content-Type") content_type: String,
        @Header("vehicleTypeId") vehicleTypeId: String
    ): Response<List<VehicleNumberResponse>>

    @POST("api/Save/UserAttendenceIn")
    suspend fun saveInPunchDetails(
        @Header("appId") appId: String,
        @Header("Content-Type") content_type: String,
        @Header("batteryStatus") batteryStatus: Int,
        @Header("imeino") imeino: String?,
        @Body inPunchRequest: InPunchRequest
    ): Response<AttendanceResponse>

    @GET("api/Get/VehicleQRDetail")
    suspend fun getVehicleQrDetails(
        @Header("AppId") appId: String,
        @Header("Content-Type") contentType: String,
        @Header("ReferanceId") referenceId: String,
        @Header("empType") empType: String,
        @Header("cur_lat") currentLat: String,
        @Header("cur_long") currentLon: String
    ):Response<VehicleQrDetailsResponse>

    @POST("api/Save/UserAttendenceOut")
    suspend fun saveOutPunchDetails(
        @Header("appId") appId: String?,
        @Header("Content-Type") content_type: String?,
        @Header("batteryStatus") batteryStatus: Int,
        @Header("Trail_id") trailId: String,
        @Header("imeino") imeino: String?,
        @Body outPunchRequest: OutPunchRequest
    ): Response<AttendanceResponse>

    @GET("api/Get/DumpYardPoint")
    suspend fun getDumpYardIds(
        @Header("AppId") appId: String,
        @Header("areaId") areaId: Int = 0
    ): Response<List<DumpYardIds>>


}