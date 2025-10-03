package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dump_yard_trip_table")
data class TripHouseData(

    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val houseId: String,
    val startDateTime: String?
)
