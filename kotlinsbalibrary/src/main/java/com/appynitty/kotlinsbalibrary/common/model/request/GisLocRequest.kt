package com.appynitty.kotlinsbalibrary.common.model.request

import com.google.gson.annotations.SerializedName

data class GisLocRequest(

    @SerializedName("id")
    val trailId: String,
    val startTs: String,
    val createTs: String,
    val updateTs: String,
    val endTs: String,
    val createUser: Int,
    val updateUser: Int,
    val geom: String,
    val offlineId: String,
    val isRunning: Int //1 when duty on 0 when off

)