package com.appynitty.kotlinsbalibrary.housescanify.model.response

import com.google.gson.annotations.SerializedName

data class EmpWorkHistoryResponse(
    var houseCollection: String?,
    @SerializedName("LiquidCollection")
    val liquidCollection: String?,
    @SerializedName("StreetCollection")
    val streetCollection: String?,
    val date: String?,
    @SerializedName("DumpYardCollection")
    val dumpYardCollection: String?,
    @SerializedName("DumpYardPlantCollection")
    val dumpYardPlantCollection: String?,
    @SerializedName("MasterPlateCollection")
    var masterPlateCollection: Int?
)
