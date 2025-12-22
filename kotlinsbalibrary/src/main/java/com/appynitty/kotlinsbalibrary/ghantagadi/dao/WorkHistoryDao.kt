package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkHistory(workHistoryData: WorkHistoryData)

    @Query("SELECT * FROM work_history_table")
    fun getWorkHistory(): LiveData<List<WorkHistoryData>>

    @Query("DELETE FROM work_history_table")
    suspend fun deleteAllWorkHistory()

    @Query("""
    SELECT *
    FROM work_history_table
    WHERE date = :date
    ORDER BY time DESC
""")
    fun getWorkHistoryByDate(date: String): Flow<List<WorkHistoryData>>

    @Query("""
    SELECT COUNT(*)
    FROM work_history_table
    WHERE date = :date
""")
    fun getWorkHistoryCountForDate(date: String):Int

    @Query("""
    DELETE FROM work_history_table
    WHERE date = :date AND Refid = :refId
""")
    suspend fun deleteByDateAndRefId(
        date: String,
        refId: String?
    )


}