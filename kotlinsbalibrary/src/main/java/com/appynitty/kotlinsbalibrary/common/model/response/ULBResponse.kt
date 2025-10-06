package com.appynitty.kotlinsbalibrary.common.model.response


import com.google.gson.annotations.SerializedName

data class ULBResponse(
    @SerializedName("Code")
    val code: Int,

    @SerializedName("Status")
    val status: String,

    @SerializedName("Message")
    val message: String,

    @SerializedName("MessageMar")
    val messageMar: String,

    @SerializedName("MessageHindi")
    val messageHindi: String,

    @SerializedName("ULBList")
    val ulbList: List<ULB>
)

data class ULB(
    @SerializedName("Appid")
    val appId: Int,

    @SerializedName("ULBName")
    val ulbName: String
)

