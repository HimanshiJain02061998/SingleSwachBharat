package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import kotlinx.coroutines.flow.Flow


@Dao
interface GarbageCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarbageCollection(garbageCollectionData: GarbageCollectionData): Long

    @Query("SELECT * FROM garbage_collection_table")
    fun getGarbageCollectionData(): Flow<List<GarbageCollectionData>>

    @Query("SELECT * FROM garbage_collection_table LIMIT :limit OFFSET :offset")
    suspend fun getGarbageCollectionDataByLimit(
        limit: Int,
        offset: Int
    ): List<GarbageCollectionData>

    @Query("DELETE FROM garbage_collection_table WHERE offlineId = :offlineId")
    suspend fun deleteGCById(offlineId: String)

    @Query("SELECT COUNT(offlineId) FROM garbage_collection_table")
    suspend fun getRowCount(): Int

    @Query("DELETE FROM garbage_collection_table")
    suspend fun deleteAllGarbageCollection()

}