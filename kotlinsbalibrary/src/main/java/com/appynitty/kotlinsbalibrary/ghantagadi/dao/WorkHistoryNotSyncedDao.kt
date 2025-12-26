package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryNewDataTemp

@Dao
interface WorkHistoryNotSyncedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkHistoryNotSynced(workHistoryNewDataTemp: WorkHistoryNewDataTemp)

    @Query("SELECT * FROM work_history_new_data_table")
    fun getWorkHistoryNotSynced(): List<WorkHistoryNewDataTemp>

    @Query("DELETE FROM work_history_new_data_table")
    suspend fun deleteAllWorkHistoryNotSynced()

    @Query("SELECT COUNT(*) FROM work_history_new_data_table")
    suspend fun getTotalCount(): Int

    @Query("""
    DELETE FROM work_history_new_data_table
    WHERE Refid = :refId
""")
    suspend fun deleteByRefId(
        refId: String?
    )

}