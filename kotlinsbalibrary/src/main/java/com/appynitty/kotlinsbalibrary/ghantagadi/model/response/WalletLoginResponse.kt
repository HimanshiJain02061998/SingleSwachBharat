package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class WalletLoginResponse(
    val code: Int,
    @SerializedName("data")
    val empWalletDetails: EmployeeWalletDetails,
    val message: String,
    val status: String
)
