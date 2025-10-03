package com.appynitty.kotlinsbalibrary.common.repository

import com.appynitty.kotlinsbalibrary.common.api.UserDetailsApi
import javax.inject.Inject

class UserDetailsRepository @Inject constructor(
    private val userDetailsApi: UserDetailsApi
) {
    suspend fun getUserDetails(
        appId: String,
        content_type: String,
        userId: String,
        typeId: String
    ) = userDetailsApi.getUserDetails(appId, content_type, userId, typeId)
}