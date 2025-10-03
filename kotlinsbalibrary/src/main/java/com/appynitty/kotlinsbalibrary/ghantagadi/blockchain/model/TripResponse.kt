package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model

import com.google.gson.annotations.SerializedName

data class TripResponse(

    val offlineId : Int?,
    val transId: String,
    val dyId: String,
    val status: String,
    val message: String,
    val messageMar: String,
    val bcTransId: String,
    @SerializedName("gvstatus")
    val tripResultStatus : Boolean

)

