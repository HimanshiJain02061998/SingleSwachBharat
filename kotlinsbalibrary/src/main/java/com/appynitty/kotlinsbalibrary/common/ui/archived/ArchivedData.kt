package com.appynitty.kotlinsbalibrary.common.ui.archived

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archived_table")
data class ArchivedData(

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val referenceId: String?,
    val errorMsg: String?,
    val errorMsgMr: String?
)
