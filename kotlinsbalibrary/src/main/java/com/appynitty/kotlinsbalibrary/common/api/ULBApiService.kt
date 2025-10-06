package com.appynitty.kotlinsbalibrary.common.api

import com.appynitty.kotlinsbalibrary.common.model.response.ULBResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ULBApiService {

    @GET("api/GetULBList")
    suspend fun getULBList(
        @Header("disid") disId: Int
    ): Response<ULBResponse>
}
