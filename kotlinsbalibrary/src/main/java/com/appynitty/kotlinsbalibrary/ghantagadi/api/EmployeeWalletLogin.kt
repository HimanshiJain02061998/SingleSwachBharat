package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.WalletLoginRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WalletLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmployeeWalletLogin {

    @POST("/api/Account/EmployeeWalletLogin")
    suspend fun isEmployeeRegistered(@Body walletLoginRequest: WalletLoginRequest): Response<WalletLoginResponse>
}