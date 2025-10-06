package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.DistrictResponse
import retrofit2.Response
import retrofit2.http.GET

interface DistrictApiService {

    @GET("api/GetDistrictList")
    suspend fun getDistrictList(): Response<DistrictResponse>
}
