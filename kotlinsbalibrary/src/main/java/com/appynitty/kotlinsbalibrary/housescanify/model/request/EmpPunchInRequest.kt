package com.appynitty.kotlinsbalibrary.housescanify.model.request

import com.google.gson.annotations.SerializedName

data class EmpPunchInRequest(
    @SerializedName("qrEmpId")
    val userId : String,
    val startTime : String ,
    val startDate : String,
    val startLat : String ,
    val startLong : String
)
