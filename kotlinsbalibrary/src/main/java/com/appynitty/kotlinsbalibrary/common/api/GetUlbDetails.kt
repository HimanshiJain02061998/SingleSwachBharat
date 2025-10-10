package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.GetDistrictListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.GetUlbListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.NearestLatLng
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface GetUlbDetails {

    @GET("api/GetDistrictList")
    suspend fun getDistrictList(
    ): Response<GetDistrictListResponse>

    @GET("api/GetULBList")
    suspend fun getUlbList(
        @Header("disid") disId: Int,
    ): Response<GetUlbListResponse>

}