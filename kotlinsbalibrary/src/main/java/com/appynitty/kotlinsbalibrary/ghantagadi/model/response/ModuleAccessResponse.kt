package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

data class ModuleAccessResponse(
    val appId: Int,
    val status: String,
    val isRewards: Boolean,
    val isWeightCollection: Boolean,
    val isDumpWeight: Boolean
)
