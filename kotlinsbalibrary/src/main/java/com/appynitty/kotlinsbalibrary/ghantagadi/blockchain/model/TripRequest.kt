package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dump_yard_offline_trip_table")
data class TripRequest(

    @PrimaryKey(autoGenerate = true)
    val offlineId: Int = 0,
    val transId: String,
    val startDateTime: String,
    val endDateTime: String,
    val userId: String,
    val dyId: String,
    val houseList: List<String>,
    val tripNo: Int,
    val vehicleNumber: String,
    val totalWetWeight: Double,
    val totalDryWeight: Double,
    val totalGcWeight: Double,
    val totalNumberOfHouses: Int

)
