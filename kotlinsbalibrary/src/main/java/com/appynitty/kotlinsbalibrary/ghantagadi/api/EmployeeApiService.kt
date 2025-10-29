package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface EmployeeApiService {
    @GET("api/Get/AvailableEmpList")
    suspend fun getAvailableEmployeeList(
        @Header("AppId") appId: String,
        @Header("EmpType") empType: String,
        @Header("userId") userId: String,
    ): Response<AvailableEmpResponse>
}