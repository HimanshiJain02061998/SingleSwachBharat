package com.appynitty.kotlinsbalibrary.ghantagadi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_history_table")
data class WorkHistoryData(
    @PrimaryKey(autoGenerate = false)
    val date: String,
    var houseCollection: String?,
    val LiquidCollection: String?,
    val StreetCollection: String?,
    val DumpYardCollection: String?,
    val DumpYardPlantCollection: String?,
    val year: String?,
    val month: String
)
