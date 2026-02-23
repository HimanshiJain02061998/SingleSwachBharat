package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
    val name: String,
    @SerializedName("offertitle")
    val offerTitle: String,
    @SerializedName("offerdiscount")
    val offerDiscount: String,
    val validDatetime: String,
    val couponCode: String,
    val offerImgUrl: String
):Parcelable
