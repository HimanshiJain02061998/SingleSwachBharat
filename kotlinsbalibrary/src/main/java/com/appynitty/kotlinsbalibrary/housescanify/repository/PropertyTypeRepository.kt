package com.appynitty.kotlinsbalibrary.housescanify.repository

import com.appynitty.kotlinsbalibrary.housescanify.api.PropertyTypeApi
import javax.inject.Inject

class PropertyTypeRepository @Inject constructor(private val propertyTypeApi: PropertyTypeApi) {

    suspend fun getAllPropertyTypes(
        appId: String
    ) = propertyTypeApi.getPropertyList(appId)
}