package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VouchersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface VouchersWebService {
    @GET("api/Citizen/MyActiveVoucher")
    suspend fun getActiveVouchers(
        @Header("WalletId") walletId: String,
        @Header("date") date: String
    ): Response<VouchersResponse>

    @GET("api/Citizen/MyClaimVoucher")
    suspend fun getClaimedVouchers(
        @Header("WalletId") walletId: String,
        @Header("date") date: String
    ): Response<VouchersResponse>

    @GET("api/Citizen/MyExpiredVoucher")
    suspend fun getExpiredVouchers(
        @Header("WalletId") walletId: String,
        @Header("date") date: String
    ): Response<VouchersResponse>
}