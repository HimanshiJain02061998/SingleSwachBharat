package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class OfferDetailsCatWiseResponse(
    val code: Int,
    @SerializedName("data")
    val offerDetailsList: List<OfferDetailsCatWise>,
    val message: String,
    val status: String
)
