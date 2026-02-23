package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyDealDetail(
    val couponCode: String,
    val offerImgUrl: String,

    @SerializedName("offerdiscount")
    val offerDiscount: String,

    @SerializedName("offerid")
    val offerId: Int,

    @SerializedName("offertitle")
    val offerTitle: String,

    @SerializedName("req_Points")
    val reqPoints: Int,

    @SerializedName("start_DateTime")
    val startDateTime: String,

    @SerializedName("valid_DateTime")
    val validDateTime: String,

    @SerializedName("offerRedeem")
    var isOfferRedeemed: Boolean
) : Parcelable
