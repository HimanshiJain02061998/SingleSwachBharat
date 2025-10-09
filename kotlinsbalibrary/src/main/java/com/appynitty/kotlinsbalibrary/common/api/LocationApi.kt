package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.request.LocationApiRequest
import com.appynitty.kotlinsbalibrary.common.model.response.LocationApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LocationApi {

    @POST("api/Save/UserLocation")
    suspend fun saveUserLocation(
        @Header("appId") appId: String?,
        @Header("Content-Type") content_type: String?,
        @Header("typeId") typeId: String?,
        @Header("EmpType") empType: String?,
        @Header("batteryStatus") batteryStatus: Int,
        @Header("imeino") imeino: String?,
        @Body userLocationPojoList: List<LocationApiRequest>
    ): Response<List<LocationApiResponse>>


}