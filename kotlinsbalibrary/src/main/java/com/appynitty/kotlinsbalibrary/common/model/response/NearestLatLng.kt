package com.appynitty.kotlinsbalibrary.common.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "nearest_houses")
data class NearestLatLng(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("ReferanceId")
    val referenceId: String,
    @SerializedName("HouseLat")
    val houseLat: String,
    @SerializedName("HouseLong")
    val houseLong: String,
    @SerializedName("Distance")
    val distance: String,
)

