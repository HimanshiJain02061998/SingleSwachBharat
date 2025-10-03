package com.appynitty.kotlinsbalibrary.housescanify.api

import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpWorkHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface EmpWorkHistoryApi {


    @GET("api/Get/QrWorkHistory")
    suspend fun getEmpWorkHistoryList(
        @Header("appId") appId: String,
        @Header("Content-Type") contentType: String,
        @Header("userId") userId: String,
        @Header("year") year: String,
        @Header("month") month: String
    ): Response<List<EmpWorkHistoryResponse>>


    @GET("api/Get/QrWorkHistoryDetails")
    suspend fun getEmpWorkHistoryDetailList(
        @Header("appId") appId: String,
        @Header("Content-Type") contentType: String,
        @Header("userId") userId: String,
        @Header("Date") fDate: String
    ): Response<List<EmpHistoryDetailsResponse>>

}

