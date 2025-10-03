package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert
    fun insert(tripRequest: TripRequest)

    @Query("SELECT * FROM  dump_yard_offline_trip_table")
    fun getAllTrips(): Flow<List<TripRequest>>

    @Query("DELETE FROM dump_yard_offline_trip_table")
    fun deleteAllTrips()

    @Query("DELETE FROM dump_yard_offline_trip_table WHERE offlineId = :offlineId")
    fun deleteSingleTrip(offlineId: Int)


}