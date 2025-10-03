package com.appynitty.kotlinsbalibrary.ghantagadi.model.request

import com.google.gson.annotations.SerializedName

data class OutPunchRequest(

    val endTime: String?,
    @SerializedName("daendDate")
    val date: String?,
    val endLat: String?,
    val endLong: String?,
    val userId: String?,
    @SerializedName("vtId")
    val vehicleId: String?,
    val vehicleNumber: String?,
    @SerializedName("EmpType")
    val empType: String?,
    var ReferanceId: String? = null
)
