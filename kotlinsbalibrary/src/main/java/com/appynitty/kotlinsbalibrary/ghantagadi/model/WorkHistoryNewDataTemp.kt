package com.appynitty.kotlinsbalibrary.ghantagadi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_history_new_data_table")
data class WorkHistoryNewDataTemp(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val Refid: String?
)