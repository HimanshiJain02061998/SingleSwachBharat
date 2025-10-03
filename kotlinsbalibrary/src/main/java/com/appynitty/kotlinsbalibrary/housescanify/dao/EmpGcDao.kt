package com.appynitty.kotlinsbalibrary.housescanify.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpGcDao {

    @Insert
    suspend fun insertEmpGc(empGarbageCollectionRequest: EmpGarbageCollectionRequest)

    @Query("SELECT * FROM emp_garbage_collctn_table")
    fun getAllEmpGcData() : Flow<List<EmpGarbageCollectionRequest>>

    @Query("SELECT * FROM emp_garbage_collctn_table LIMIT :limit OFFSET :offset")
    suspend fun getGarbageCollectionDataByLimit(
        limit: Int,
        offset: Int
    ): List<EmpGarbageCollectionRequest>

    @Query("SELECT COUNT(offlineId) FROM emp_garbage_collctn_table")
    suspend fun getRowCount(): Int

    @Query("DELETE FROM emp_garbage_collctn_table WHERE offlineId = :offlineId")
    suspend fun deleteGCById(offlineId: String)

    @Query("SELECT * FROM emp_garbage_collctn_table WHERE offlineId = :offlineId")
    fun getGcById(offlineId: String): Flow<EmpGarbageCollectionRequest>

}