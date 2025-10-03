package com.appynitty.kotlinsbalibrary.housescanify.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyTypeDao {

    @Insert
    suspend fun insertPropertyDao(propertyType: PropertyType)

    @Query("SELECT * FROM property_type_table")
    fun getAllPropertyTypes() : Flow<List<PropertyType>>

    @Query("DELETE FROM property_type_table")
    suspend fun deleteAllProperties()

    @Query("DELETE FROM property_type_table WHERE id = :id")
    suspend fun deleteProperty(id : Int)


}