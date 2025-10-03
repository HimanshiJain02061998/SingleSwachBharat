package com.appynitty.kotlinsbalibrary.housescanify.model.response

import com.google.gson.annotations.SerializedName

data class MasterPlateExist(
    @SerializedName("MasterId")
    val masterId: Int,

    @SerializedName("ReferenceID")
    val referenceID: String,

    @SerializedName("BunchList")
    val bunchList: String,

    @SerializedName("IsReScan")
    val isReScan: Boolean,
    val status: String,
    val message: String,
    val messageMar: String
)

/*"status":"error","message":"Invalid Master Plate ID","messageMar":"अवैध मास्टर प्लेट आयडी"*/