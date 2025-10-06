package com.appynitty.kotlinsbalibrary.common.model.response

data class DistrictResponse(
    val Code: Int,
    val Status: String,
    val Message: String,
    val MessageMar: String,
    val MessageHindi: String,
    val DistrictList: List<District>
)

data class District(
    val Disid: Int,
    val DistrictName: String
)

