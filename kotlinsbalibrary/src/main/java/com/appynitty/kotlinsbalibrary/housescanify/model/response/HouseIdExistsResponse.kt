package com.appynitty.kotlinsbalibrary.housescanify.model.response

import com.google.gson.annotations.SerializedName

data class HouseIdExistsResponse(
    val status: String,
    val message: String,
    val messageMar: String,
    @SerializedName("HouseId")
    val houseId: Int,
    @SerializedName("ReferenceID")
    val referenceID: String,
    @SerializedName("IsExists")
    val isExists: Boolean
)