package com.appynitty.kotlinsbalibrary.common.model.response

import com.google.gson.annotations.SerializedName

data class LocationApiResponse(

    @SerializedName("isAttendenceOff")
    val isAttendanceOff: Boolean?,
    val message: String?,
    val messageMar: String?,
    val status: String?,
    @SerializedName("ID")
    val offlineId: Int?

)
