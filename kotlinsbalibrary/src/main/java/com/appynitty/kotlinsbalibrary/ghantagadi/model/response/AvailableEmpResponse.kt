package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class AvailableEmpResponse(
    val Code: Int,
    val Status: String,
    val Message: String,
    val MessageMar: String?,
    val MessageHin: String?,
    val availableEmpList: List<AvailableEmpItem>
)
@Parcelize
data class AvailableEmpItem(
    val userid: Int,
    val EmployeeName: String
): Parcelable

