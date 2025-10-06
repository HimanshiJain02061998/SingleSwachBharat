package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.ULBApiService
import com.appynitty.kotlinsbalibrary.common.model.response.ULBResponse
import retrofit2.Response
import javax.inject.Inject

class ULBRepository @Inject constructor(
    private val apiService: ULBApiService
) {

    suspend fun getULBList(disId: Int): Response<ULBResponse> {
        return apiService.getULBList(disId)
    }
}
