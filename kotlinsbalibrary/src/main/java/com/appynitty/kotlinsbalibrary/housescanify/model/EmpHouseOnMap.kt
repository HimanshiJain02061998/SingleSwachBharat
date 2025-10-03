package com.appynitty.kotlinsbalibrary.housescanify.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "house_on_map_history")
data class EmpHouseOnMap(

    @PrimaryKey(autoGenerate = false)
    val referenceId: String,
    val latitude: String?,
    val longitude: String?,
    val gcType : Int?

)
