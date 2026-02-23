package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

data class AboutCoinsResponse(
    val aboutCoinDetails: List<AboutCoinDetail>,
    val code: Int,
    val message: String,
    val status: String
)