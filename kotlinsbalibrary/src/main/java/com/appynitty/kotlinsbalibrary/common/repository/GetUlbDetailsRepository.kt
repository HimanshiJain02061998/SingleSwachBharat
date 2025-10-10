package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.GetUlbDetails
import com.appynitty.kotlinsbalibrary.common.api.NearestLatLngApi
import javax.inject.Inject

class GetUlbDetailsRepository @Inject constructor(
    private val api: GetUlbDetails
) {
    suspend fun getDistrictList(
    ) = api.getDistrictList()

    suspend fun getUlbList(
        disId: Int,
    ) = api.getUlbList(disId)
}