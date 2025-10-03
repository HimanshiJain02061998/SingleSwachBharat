package com.appynitty.kotlinsbalibrary.common.model.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "location_table")
data class LocationApiRequest(

    @PrimaryKey(autoGenerate = true)
    @SerializedName("OfflineId")
    val offlineId: Int = 0,
    val userId: String,
    val lat: String,
    @SerializedName("Long")
    val longitude: String,
    val datetime: String,
    val distance: String,
    val isOffline: Boolean
)
