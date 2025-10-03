package com.appynitty.kotlinsbalibrary.housescanify.model.request

import com.google.gson.annotations.SerializedName

data class EmpPunchOutRequest(

    @SerializedName("qrEmpId")
    val userId : String,
    val endTime : String ,
    val endDate : String,
    val endLat : String ,
    val endLong : String

)