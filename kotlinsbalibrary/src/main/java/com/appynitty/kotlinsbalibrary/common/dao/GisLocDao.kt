package com.appynitty.kotlinsbalibrary.common.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.common.model.GisLatLong
import kotlinx.coroutines.flow.Flow

@Dao
interface GisLocDao {

    @Insert
    suspend fun insertGisLatLong(gisLatLong: GisLatLong)

    @Query("SELECT * FROM gis_location_table")
    fun getAllGisLatLongs(): Flow<List<GisLatLong>>

    @Query("DELETE FROM gis_location_table")
    suspend fun deleteAllGisLatLongs()

    @Query("DELETE FROM gis_location_table WHERE id = :offlineId")
    suspend fun deleteLocationById(offlineId: String)
}