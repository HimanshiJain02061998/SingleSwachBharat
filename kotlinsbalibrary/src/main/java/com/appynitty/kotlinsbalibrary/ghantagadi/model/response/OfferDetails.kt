package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class OfferDetails(
    val code: Int,
    @SerializedName("data")
    val offerData: List<OfferData>,
    val message: String,
    val status: String
)
