package com.appynitty.kotlinsbalibrary.common.model.response

import com.google.gson.annotations.SerializedName

data class ForceUpdateResponse(
    val status: String?,
    val message: String?,
    @SerializedName("applink")
    val appDownloadLink: String?,
    val isForceUpdate: Boolean?,
)
