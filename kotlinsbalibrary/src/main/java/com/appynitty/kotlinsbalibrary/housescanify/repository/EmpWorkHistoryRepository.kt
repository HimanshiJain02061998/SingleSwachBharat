package com.appynitty.kotlinsbalibrary.housescanify.repository

import com.appynitty.kotlinsbalibrary.housescanify.api.EmpWorkHistoryApi
import javax.inject.Inject

class EmpWorkHistoryRepository @Inject constructor(
    private val empHistoryApi : EmpWorkHistoryApi
){
    suspend fun getWorkHistoryList(
        appId: String,
        contentType: String,
        userId: String,
        year: String,
        month: String,
    ) = empHistoryApi.getEmpWorkHistoryList(appId, contentType, userId, year, month)

    suspend fun getWorkHistoryDetailList(
        appId: String,
        contentType: String,
        userId: String,
        fDate: String
    ) = empHistoryApi.getEmpWorkHistoryDetailList(appId, contentType, userId, fDate)

}