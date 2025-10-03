package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.LoginApi
import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import javax.inject.Inject

/**
 *  CREATED BY SANATH GOSAVI
 */

class LoginRepository @Inject constructor(
    private val loginAPI: LoginApi
) {
    suspend fun saveLoginDetails(
        appId: String,
        contentType: String,
        loginRequest: LoginRequest
    ) = loginAPI.saveLoginDetails(appId, contentType, loginRequest)

}