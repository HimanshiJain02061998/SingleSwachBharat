package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RewardsRegRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RewardsRegResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RewardsEmpRegistration {

    @POST("/api/Account/EmployeeWalletRegistration")
    suspend fun registerUserForRewardsSystem(@Body rewardsRegRequest: RewardsRegRequest): Response<RewardsRegResponse>
}