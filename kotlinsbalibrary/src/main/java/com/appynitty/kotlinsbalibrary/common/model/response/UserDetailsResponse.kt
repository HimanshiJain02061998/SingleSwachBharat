package com.appynitty.kotlinsbalibrary.common.model.response

import com.google.gson.annotations.SerializedName


data class UserDetailsResponse(

    @SerializedName("userId")
    val employeeId: String?,
    val name: String?,
    val profileImage: String?,
    val address: String?,
    val type: String?,
    val mobileNumber: String?,
    val nameMar: String?,
    val bloodGroup: String?,

    val partnerName: String?,
    val partnerCode: String?

)
