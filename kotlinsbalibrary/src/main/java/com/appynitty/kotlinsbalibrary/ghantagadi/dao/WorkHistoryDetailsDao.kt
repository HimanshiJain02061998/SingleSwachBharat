package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryDetailsData
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkHistoryDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkHistoryDetails(workHistoryDetailsData: WorkHistoryDetailsData)

    @Query("SELECT * FROM work_history_details_table")
    fun getWorkHistoryDetails(): LiveData<List<WorkHistoryDetailsData>>

    @Query("DELETE FROM work_history_details_table")
    suspend fun deleteAllWorkHistoryDetails()

    @Query("""
    SELECT *
    FROM work_history_details_table
    WHERE date = :date
    ORDER BY time DESC
""")
    fun getWorkHistoryDetailsByDate(date: String): Flow<List<WorkHistoryDetailsData>>

    @Query("""
    SELECT COUNT(*)
    FROM work_history_details_table
    WHERE date = :date
""")
    fun getWorkHistoryDetailsCountForDate(date: String):Int

    @Query("""
    DELETE FROM work_history_details_table
    WHERE date = :date AND Refid = :refId
""")
    suspend fun deleteByDateAndRefId(
        date: String,
        refId: String?
    )

    @Upsert
    suspend fun upsertWorkHistoryDetails(list: List<WorkHistoryDetailsData>)

}