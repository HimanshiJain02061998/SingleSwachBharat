package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import com.google.gson.annotations.SerializedName

data class GarbageCollectionResponse(

    @SerializedName("ID")
    val offlineId: String?,
    val referenceID: String?,
    val message: String?,
    val messageMar: String?,
    val status: String?
)

