package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class TransactionHistoryResponse(
    val code: Int,
    @SerializedName("data")
    val transactionList: List<Transaction>,
    val message: String,
    val status: String
)
