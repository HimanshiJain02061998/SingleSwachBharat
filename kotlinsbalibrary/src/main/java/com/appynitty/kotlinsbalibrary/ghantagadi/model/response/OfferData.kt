package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class OfferData(
    @SerializedName("coupen_Code")
    val couponCode: String,

    val offerDesc: String,
    val offerDiscount: String,
    val offerId: Int,
    val offerImgUrl: String,
    val offerRedeem: Boolean,
    val offerTerms: String,
    val offerTitle: String,

    @SerializedName("offer_EndTime")
    val offerEndTime: String,

    @SerializedName("offer_IsActive")
    val offerIsActive: Boolean,

    @SerializedName("offer_StartTime")
    val offerStartTime: String,

    @SerializedName("req_Points")
    val reqPoints: Int
)
