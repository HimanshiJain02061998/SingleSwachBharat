package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.TransactionHistoryWebService
import javax.inject.Inject

class TransactionHistoryRepo @Inject constructor(private val transactionHistWebService: TransactionHistoryWebService) {
    suspend fun getTransactionHistory(walletId: String, transactionDate: String) =
        transactionHistWebService.getTransactionHistory( walletId, transactionDate)
}