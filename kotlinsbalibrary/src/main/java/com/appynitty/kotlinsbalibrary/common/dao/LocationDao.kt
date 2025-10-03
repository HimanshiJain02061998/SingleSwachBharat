package com.appynitty.kotlinsbalibrary.common.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.common.model.request.LocationApiRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationApiRequest: LocationApiRequest)

    @Query("SELECT * FROM location_table")
    fun getAllLocationData(): Flow<List<LocationApiRequest>>

    @Query("DELETE FROM location_table WHERE offlineId =:offlineId")
    suspend fun deleteLocationById(offlineId: String)
}