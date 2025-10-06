package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.DistrictApiService
import com.appynitty.kotlinsbalibrary.common.model.response.DistrictResponse
import retrofit2.Response
import javax.inject.Inject

class DistrictRepository @Inject constructor(
    private val districtApiService: DistrictApiService
) {

    suspend fun getDistrictList(): Response<DistrictResponse> {
        return districtApiService.getDistrictList()
    }
}
