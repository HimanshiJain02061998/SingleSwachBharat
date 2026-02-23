package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.EmployeeWalletLogin
import com.appynitty.kotlinsbalibrary.ghantagadi.api.RewardsEmpRegistration
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RewardsRegRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.WalletLoginRequest
import javax.inject.Inject

class RewardsWalletRepository @Inject constructor(
    private val employeeWalletLogin: EmployeeWalletLogin,
    private val rewardsEmpRegistration: RewardsEmpRegistration
) {
    suspend fun isEmployeeRegistered(walletLoginRequest: WalletLoginRequest) =
        employeeWalletLogin.isEmployeeRegistered(walletLoginRequest)

    suspend fun registerEmployee(registrationRequest: RewardsRegRequest) =
        rewardsEmpRegistration.registerUserForRewardsSystem(registrationRequest)
}