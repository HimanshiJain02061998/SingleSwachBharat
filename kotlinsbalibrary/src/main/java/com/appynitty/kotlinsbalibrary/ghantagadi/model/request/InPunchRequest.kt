package com.appynitty.kotlinsbalibrary.ghantagadi.model.request

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
import com.google.gson.annotations.SerializedName

data class InPunchRequest(

    val startTime: String?,
    @SerializedName("daDate")
    val date: String?,
    val startLat: String?,
    val startLong: String?,
    val userId: String?,
    @SerializedName("vtId")
    var vehicleType: String?,
    var vehicleNumber: String?,
    @SerializedName("EmpType")
    val empType: String?,
    var ReferanceId: String? = null,
    @SerializedName("memberUserIds")
    val memberUserIds: List<Int>? = null // ✅ Only list of Ints
)
