package com.appynitty.kotlinsbalibrary.ghantagadi.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class WorkHistoryDetailsResponse(

    val time: String?,
    val Refid: String?,
    val name: String?,
    val vehicleNumber: String?,
    val areaName: String?,
    val type: String?

): Parcelable

//"time": "11:19",
//"date": "12/05/2022",
//"type": 1,
//"houseNo": "HPSBA1001",
//"dumpYardNo": null,
//"pointNo": null,
//"liquidNo": null,
//"streetNo": null