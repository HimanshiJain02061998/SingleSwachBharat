package com.appynitty.kotlinsbalibrary.housescanify.model.response

import com.google.gson.annotations.SerializedName

data class EmpGcResponse(

    @SerializedName("ID")
    val offlineId: String?,
    val referenceID: String?,
    val message: String?,
    val messageMar: String?,
    val status: String?,
    val gismessage: String?,
    val giserrorMessages: String?,
    val houseid: String?,
    val dyId: String?,
    val code: Int?,
    val IsExixts: String?,
    val applink: String?

)
