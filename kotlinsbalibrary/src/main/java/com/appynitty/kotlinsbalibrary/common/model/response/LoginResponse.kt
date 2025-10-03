package com.appynitty.kotlinsbalibrary.common.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class LoginResponse(
    val userId: String,
    val type: String,
    val typeId: String,
    val status: String,
    val message: String?,
    val messageMar: String?,
    val gtFeatures: Boolean,
    val EmpType: String
) : Parcelable
