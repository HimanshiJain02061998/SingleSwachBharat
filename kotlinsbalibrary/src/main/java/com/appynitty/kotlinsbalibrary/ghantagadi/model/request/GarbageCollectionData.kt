package com.appynitty.kotlinsbalibrary.ghantagadi.model.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "garbage_collection_table")
data class GarbageCollectionData(

    @SerializedName("OfflineId")
    @PrimaryKey(autoGenerate = true)
    val offlineId: Int,
    @SerializedName("ReferenceID")
    val referenceId: String,
    val userId: String,
    @SerializedName("Lat")
    val latitude: String?,
    @SerializedName("Long")
    val longitude: String?,
    val vehicleNumber: String,

    //garbage collection type
    val gcType: String,
    //garbage type
    val garbageType: String?,
    val gcDate: String,
    val batteryStatus: String,
    @SerializedName("Distance")
    val distance: String,
    @SerializedName("IsLocation")
    val isLocation: Boolean,
    val isOffline: Boolean?,
    val empType: String,
    val note: String?,

    @SerializedName("gpBeforImage")
    var gpBeforeImage: String?,
    var gpAfterImage: String?,
    @SerializedName("gpBeforImageTime")
    val gpBeforeImageTime: String?,

    // only for dump yard
    val totalGcWeight: String?,
    val totalDryWeight: String?,
    val totalWetWeight: String?

)