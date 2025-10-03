package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleTypeResponse(
    val description: String?,
    val vtId: String?,
    val descriptionMar: String?
) : Parcelable
