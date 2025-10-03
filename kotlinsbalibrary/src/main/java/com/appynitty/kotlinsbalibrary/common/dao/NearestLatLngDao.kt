package com.appynitty.kotlinsbalibrary.common.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.common.model.GisLatLong
import com.appynitty.kotlinsbalibrary.common.model.response.NearestLatLng
import kotlinx.coroutines.flow.Flow

@Dao
interface  NearestLatLngDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNearestHouse(nearestLatLng: NearestLatLng)

    @Query("SELECT * FROM nearest_houses")
    fun getNearestHouses(): Flow<List<NearestLatLng>>


    @Query("SELECT * FROM nearest_houses WHERE referenceId =:referenceId ")
    fun getNearestHouseByRefId(referenceId: String): NearestLatLng?

    @Query("DELETE FROM nearest_houses")
    suspend fun deleteAllNearestHouses()

    @Query("DELETE FROM nearest_houses WHERE referenceId = :referenceId")
    suspend fun deleteNearestHouseById(referenceId: String)

}