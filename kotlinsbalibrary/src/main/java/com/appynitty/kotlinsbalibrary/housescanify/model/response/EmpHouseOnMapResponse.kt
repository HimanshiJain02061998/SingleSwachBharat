package com.appynitty.kotlinsbalibrary.housescanify.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmpHouseOnMapResponse(

    @SerializedName("RefferenceId")
    val referenceId: String?,
    @SerializedName("Lat")
    val latitude: String? ,
    @SerializedName("Long")
    val longitude: String?,
    val gcType : Int?
) : Parcelable





