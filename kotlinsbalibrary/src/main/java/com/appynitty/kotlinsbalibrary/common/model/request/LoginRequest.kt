package com.appynitty.kotlinsbalibrary.common.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("EmpType")
    val empType: String,
    val userPassword: String,
    val userLoginId: String,
    val imiNo: String
)
