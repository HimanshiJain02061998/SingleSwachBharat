package com.appynitty.kotlinsbalibrary.common.dao

import androidx.room.*
import com.appynitty.kotlinsbalibrary.common.model.UserData
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userData: UserData)

    @Query("SELECT * FROM user_table")
    fun gerUserData(): Flow<UserData?>

    @Delete
    suspend fun deleteUserData(userData: UserData)

    @Query("DELETE FROM user_table")
    suspend fun deleteAllUserData()

}