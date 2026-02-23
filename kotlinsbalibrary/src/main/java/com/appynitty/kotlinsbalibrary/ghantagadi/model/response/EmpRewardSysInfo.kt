package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class EmpRewardSysInfo(
    val balCoins: Int,
    val eId: Int,
    val emailId: String,
    val firstName: String,
    val lastName: String,
    val mobileNo: String,
    val username: String,
    @SerializedName("wallet_Id")
    val walletId: String
)