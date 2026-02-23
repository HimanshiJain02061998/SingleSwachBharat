package com.appynitty.kotlinsbalibrary.ghantagadi.model.request

data class RewardsRegRequest(
    val EUsername: String,
    val address: String,
    val appId: Int,
    val emailId: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val mobileNo: String
)
