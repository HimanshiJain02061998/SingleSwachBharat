package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.LoginApi
import com.appynitty.kotlinsbalibrary.common.api.NearestLatLngApi
import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import javax.inject.Inject

class NearestLatLngRepository @Inject constructor(
    private val api: NearestLatLngApi
) {
    suspend fun getNearestLatLongs(
        appId: String,
        sourceLat: String,
        sourceLong: String,
        userId : Int
    ) = api.getNearestLatLongs(appId, sourceLat, sourceLong,userId)

}