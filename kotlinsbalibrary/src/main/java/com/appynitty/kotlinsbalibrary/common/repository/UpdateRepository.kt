package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.LocationApi
import com.appynitty.kotlinsbalibrary.common.api.UpdateApi
import com.appynitty.kotlinsbalibrary.common.model.response.ForceUpdateResponse
import retrofit2.Response
import retrofit2.http.Header
import javax.inject.Inject

class UpdateRepository @Inject constructor(private val updateApi: UpdateApi) {

    suspend fun getVersionUpdate(
        appId: String,
        version: Int
    ) = updateApi.getVersionUpdate(appId, version)

}