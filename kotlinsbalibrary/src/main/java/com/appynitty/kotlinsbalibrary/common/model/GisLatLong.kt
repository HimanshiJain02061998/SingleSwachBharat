package com.appynitty.kotlinsbalibrary.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gis_location_table")
class GisLatLong(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val startTs: String
)
