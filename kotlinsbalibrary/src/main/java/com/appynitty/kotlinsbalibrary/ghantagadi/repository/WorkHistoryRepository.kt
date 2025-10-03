package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.WorkHistoryApi
import javax.inject.Inject

class WorkHistoryRepository @Inject constructor(
    private val workHistoryApi: WorkHistoryApi
) {
    suspend fun getWorkHistoryList(
        appId: String,
        userId: String,
        year: String,
        month: String,
        empType: String
    ) = workHistoryApi.getWorkHistoryList(appId, userId, year, month, empType)

    suspend fun getWorkHistoryDetailList(
        appId: String,
        userId: String,
        fDate: String,
        languageId: String
    ) = workHistoryApi.getWorkHistoryDetailList(appId, userId, fDate, languageId)

}