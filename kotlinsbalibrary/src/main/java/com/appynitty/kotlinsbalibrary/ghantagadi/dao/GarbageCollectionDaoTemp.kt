package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionDataTemp
import kotlinx.coroutines.flow.Flow


@Dao
interface GarbageCollectionDaoTemp {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarbageCollection(garbageCollectionData: GarbageCollectionDataTemp): Long

    @Query("""
    UPDATE garbage_collection_table_temp
    SET isUploaded = 1
    WHERE offlineId = :offlineId
""")
    suspend fun markAsUploaded(offlineId: Int): Int

    @Query("SELECT * FROM garbage_collection_table_temp")
    fun getGarbageCollectionData(): Flow<List<GarbageCollectionDataTemp>>

    @Query("SELECT * FROM garbage_collection_table_temp LIMIT :limit OFFSET :offset")
    suspend fun getGarbageCollectionDataByLimit(
        limit: Int,
        offset: Int
    ): List<GarbageCollectionData>

    @Query("DELETE FROM garbage_collection_table_temp WHERE offlineId = :offlineId")
    suspend fun deleteGCById(offlineId: String)

    @Query("SELECT COUNT(offlineId) FROM garbage_collection_table_temp")
    suspend fun getRowCount(): Int

    @Query("DELETE FROM garbage_collection_table_temp")
    suspend fun deleteAllGarbageCollection()

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM garbage_collection_table_temp 
        WHERE referenceId = :referenceId
    )
""")
    suspend fun isIDPresent(referenceId: String): Boolean

    @Query("""
    DELETE FROM garbage_collection_table_temp
    WHERE referenceId = :referenceId
""")
    suspend fun deleteByReferenceId(referenceId: String): Int

}