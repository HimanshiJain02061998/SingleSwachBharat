package com.appynitty.kotlinsbalibrary.ghantagadi.model.request

import com.google.gson.annotations.SerializedName

data class WalletLoginRequest(
    @SerializedName("AppId")
    val appId: Int,
    @SerializedName("EUsername")
    val eUserName: String
)
