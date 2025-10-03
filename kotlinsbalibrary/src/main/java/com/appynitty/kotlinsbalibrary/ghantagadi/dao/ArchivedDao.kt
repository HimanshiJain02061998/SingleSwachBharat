package com.appynitty.kotlinsbalibrary.ghantagadi.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedData
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchivedData(archivedData: ArchivedData)

    @Query("SELECT * FROM archived_table")
    fun getArchivedData(): LiveData<List<ArchivedData>>

    @Query("DELETE FROM archived_table")
    suspend fun deleteAllArchivedData()

}