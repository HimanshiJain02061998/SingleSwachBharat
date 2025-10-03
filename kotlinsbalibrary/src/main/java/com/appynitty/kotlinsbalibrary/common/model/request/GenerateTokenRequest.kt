package com.appynitty.kotlinsbalibrary.common.model.request

data class GenerateTokenRequest(val username: String, val password: String, val grant_type: String)
