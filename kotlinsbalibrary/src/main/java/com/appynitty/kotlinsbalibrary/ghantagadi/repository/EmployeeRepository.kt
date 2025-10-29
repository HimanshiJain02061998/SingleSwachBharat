package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.EmployeeApiService
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
import javax.inject.Inject

class EmployeeRepository @Inject constructor(
    private val apiService: EmployeeApiService
) {

    suspend fun fetchAvailableEmployees(  appId: String, empType: String, userId: String,): List<AvailableEmpItem>? {
        return try {
            val response = apiService.getAvailableEmployeeList(
                appId,empType,userId
            )
            if (response.isSuccessful) {
                response.body()?.availableEmpList
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
