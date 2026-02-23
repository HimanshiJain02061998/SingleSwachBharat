package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.ModuleAccessWebService
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.ModuleAccessResponse
import javax.inject.Inject

class ModuleAccessRepo @Inject constructor(private val moduleAccessWebService: ModuleAccessWebService) {
    suspend fun getModuleAccessData(appId: String, empType: String?): ModuleAccessResponse? {
        return try {
            val response = moduleAccessWebService.getModuleAccess(appId, empType)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}