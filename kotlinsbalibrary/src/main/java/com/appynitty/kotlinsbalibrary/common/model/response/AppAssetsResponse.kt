package com.appynitty.kotlinsbalibrary.common.model.response

data class AppAssetsResponse(
    val Code: Int,
    val Status: String,
    val Message: String,
    val AppAssetsList: AppAssetsList
)

data class AppAssetsList(
    val appIcon: String
)