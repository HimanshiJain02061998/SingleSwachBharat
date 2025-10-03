package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.UserTravelLoc
import kotlinx.coroutines.flow.Flow


@Dao
interface UserTravelLocDao {
    @Insert
    suspend fun insertUserTravelLatLong(userTravelLoc: UserTravelLoc)

    @Query("SELECT * FROM user_travel_location_table")
    fun getAllUserTravelLatLongs(): Flow<List<UserTravelLoc>>

    @Query("DELETE FROM user_travel_location_table")
    suspend fun deleteAllUserTravelLatLongs()

}