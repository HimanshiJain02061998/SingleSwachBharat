package com.appynitty.kotlinsbalibrary.ghantagadi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_history_new_data_table")
data class WorkHistoryNewDataTemp(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val Refid: String?,
    val time: String?,
    val name: String?,
    val vehicleNumber: String?,
    val areaName: String?,
    val type: String?,
)