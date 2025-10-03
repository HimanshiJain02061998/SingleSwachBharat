package com.appynitty.kotlinsbalibrary.housescanify.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("property_type_table")
data class PropertyType
    (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val Property_Id: Int?,
    val Property_Type: String?,
    val Property_Type_Mar: String?,
    val Property_Type_Hin: String?,
    var isChecked: Boolean = false

)



