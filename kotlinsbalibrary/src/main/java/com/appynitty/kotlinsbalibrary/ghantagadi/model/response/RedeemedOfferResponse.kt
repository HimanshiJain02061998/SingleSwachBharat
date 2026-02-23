package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class RedeemedOfferResponse(
    val code: Int,
    val status: String,
    val message: String,
    @SerializedName("redeemofferdata")
    val redeemOfferData: RedeemOfferData
)
