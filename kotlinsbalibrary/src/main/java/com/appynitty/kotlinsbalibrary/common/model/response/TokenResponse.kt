package com.appynitty.kotlinsbalibrary.common.model.response

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: String,
    val userName: String
)