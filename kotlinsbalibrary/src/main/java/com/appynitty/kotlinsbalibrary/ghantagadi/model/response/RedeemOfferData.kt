package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RedeemOfferData(
    val offerTitle: String,
    val discountTitle: String,
    val couponCode: String,
    val validDatetime: String
) : Parcelable
