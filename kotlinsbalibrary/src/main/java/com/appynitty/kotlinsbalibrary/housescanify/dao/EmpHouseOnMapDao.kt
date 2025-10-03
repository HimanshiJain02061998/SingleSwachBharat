package com.appynitty.kotlinsbalibrary.housescanify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.housescanify.model.EmpHouseOnMap
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpHouseOnMapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseOnMap(empHouseOnMap: EmpHouseOnMap)

    @Query("SELECT * FROM house_on_map_history")
    fun getAllHouseOnMapData() : Flow<List<EmpHouseOnMap>>

    @Query("DELETE FROM house_on_map_history")
    suspend fun deleteAllHouseOnMapData()

    @Query("DELETE FROM house_on_map_history WHERE referenceId = :referenceId")
    suspend fun deleteHouseOnMapById(referenceId :String)

}