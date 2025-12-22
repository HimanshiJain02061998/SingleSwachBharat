package com.appynitty.kotlinsbalibrary.ghantagadi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_history_details_table")
data class WorkHistoryDetailsData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: String,
    val Refid: String?,
    val time: String?,
    val name: String?,
    val vehicleNumber: String?,
    val areaName: String?,
    val type: String?,
    val isSynced: Boolean?
)