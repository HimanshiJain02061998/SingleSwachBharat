package com.appynitty.kotlinsbalibrary.housescanify.model.request

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "emp_garbage_collctn_table")
data class EmpGarbageCollectionRequest(

    @PrimaryKey(autoGenerate = true)
    @SerializedName("OfflineId")
    val offlineId: Int,
    @SerializedName("Lat")
    val latitude: String,
    @SerializedName("Long")
    val longitude: String,
    val date: String,
    val gcType: String,
    val userId: String,
    @SerializedName("referanceId")
    val referenceId: String,
    @SerializedName("QRCodeImage")
    var referenceImage: String?,
    var HouseImage: String?,
    val geom: String,
    val createTs: String,
    @SerializedName("new_const")
    val newConstruction: String,
    // 0 if old construction else new house ( 1 )

    val Address: String,
    val Name: String,
    val NameMar: String,
    val areaId: Int,
    val cType: String,
    val houseNumber: String,
    val mobileno: String,
    val wardId: Int,
    val zoneId: Int,
    val property_typeid: Int,
    @SerializedName("BunchList")
    val bunchList: String? = null,
    @SerializedName("IsImgUpdate")
    val isImageUpdated: Boolean? = null,
    @SerializedName("IsBunchUpdate")
    val isBunchUpdated: Boolean? = null


)
