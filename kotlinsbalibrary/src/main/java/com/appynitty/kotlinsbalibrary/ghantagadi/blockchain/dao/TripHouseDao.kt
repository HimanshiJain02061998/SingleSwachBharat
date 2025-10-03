package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripHouseData
import kotlinx.coroutines.flow.Flow


@Dao
interface TripHouseDao {

    @Insert
    suspend fun insert(tripHouseData: TripHouseData)

    @Query("SELECT * FROM  dump_yard_trip_table")
    fun getAllScannedHouse(): Flow<List<TripHouseData>>

    @Query("DELETE FROM dump_yard_trip_table")
    suspend fun deleteScannedHouse()

    @Delete
    fun deleteSingleScannedHouse(tripHousePojo: TripHouseData)


    @Query("DELETE FROM dump_yard_trip_table WHERE id =:id")
    suspend fun deleteById(id : Int)

}