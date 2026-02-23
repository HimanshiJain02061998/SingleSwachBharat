package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.TransactionHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TransactionHistoryWebService {

    @GET("/api/Employee/GetEmpCoinsTransactionHistory")
    suspend fun getTransactionHistory(
        @Header("WalletId") walletId: String,
        @Header("date") transactionDate: String
    ): Response<TransactionHistoryResponse>
}