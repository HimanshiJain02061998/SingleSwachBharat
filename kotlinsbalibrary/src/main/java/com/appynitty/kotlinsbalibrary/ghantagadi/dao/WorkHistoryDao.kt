package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData

@Dao
interface WorkHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkHistory(workHistoryData: WorkHistoryData)

    @Query("SELECT * FROM work_history_table")
    fun getWorkHistory(): LiveData<List<WorkHistoryData>>

    @Query("DELETE FROM work_history_table")
    suspend fun deleteAllWorkHistory()

}