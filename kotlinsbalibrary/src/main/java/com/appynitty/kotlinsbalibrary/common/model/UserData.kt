package com.appynitty.kotlinsbalibrary.common.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "user_table")
data class UserData(
    //login response
    @PrimaryKey(autoGenerate = false)
    val userId: String,
    val userTypeId: String,
    val employeeType: String,

    //get user response
    val userName: String?,
    val userNameMar: String?,
    val employeeId: String?,
    val userProfileImage: String?,
    val userAddress: String?,
    val userMobileNo: String?,
    val userBloodGroup: String?,
    val userpartnerName: String?,
    val userpartnerCode: String?
) : Parcelable