package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.LocationApi
import com.appynitty.kotlinsbalibrary.common.model.request.LocationApiRequest
import retrofit2.http.Header
import javax.inject.Inject

class LocationRepository @Inject constructor(private val locationApi: LocationApi) {

    suspend fun saveUserLocationToApi(
        appId: String,
        content_type: String,
        typeId: String,
        empType: String,
        batteryStatus: Int,
        imeino: String?,
        userLocationPojoList: List<LocationApiRequest>
    ) = locationApi.saveUserLocation(
        appId,
        content_type,
        typeId,
        empType,
        batteryStatus,
        imeino,
        userLocationPojoList
    )

}