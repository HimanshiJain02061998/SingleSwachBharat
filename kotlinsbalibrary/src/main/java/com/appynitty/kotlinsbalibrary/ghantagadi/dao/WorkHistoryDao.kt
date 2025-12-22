package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkHistory(workHistoryData: WorkHistoryData)

    @Query("SELECT * FROM work_history_table")
    fun getWorkHistory(): Flow<List<WorkHistoryData>>

    @Query("DELETE FROM work_history_table")
    suspend fun deleteAllWorkHistory()

    @Query("""
    SELECT * FROM work_history_table 
    WHERE year = :year AND month = :month
""")
     fun getWorkHistoryByMonthYear(
        year: String,
        month: String
    ): Flow<List<WorkHistoryData>>

    @Query("""
    SELECT COUNT(*) 
    FROM work_history_table 
    WHERE year = :year AND month = :month
""")
    suspend fun getCountByMonthYear(
        year: String,
        month: String
    ): Int

    @Upsert
    suspend fun upsertWorkHistory(data: WorkHistoryData)

}
