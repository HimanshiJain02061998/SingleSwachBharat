package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface WorkHistoryApi {

    @GET("api/Get/WorkHistory")
    suspend fun getWorkHistoryList(
        @Header("appId") appId: String,
        @Header("userId") userId: String,
        @Header("year") year: String,
        @Header("month") month: String,
        @Header("EmpType") empType: String
    ): Response<List<WorkHistoryResponse>>


    @GET("api/Get/WorkHistory/Details")
    suspend fun getWorkHistoryDetailList(
        @Header("appId") appId: String,
        @Header("userId") userId: String,
        @Header("fdate") fDate: String,
        @Header("LanguageId") languageId: String?
    ): Response<List<WorkHistoryDetailsResponse>>

}