package com.appynitty.kotlinsbalibrary.common.di

import android.util.Log
import com.appynitty.kotlinsbalibrary.common.api.TokenApi
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AppAuthenticator @Inject constructor(
    private val sessionManager: SessionDataStore,
    private val tokenApi : TokenApi
    ) : Authenticator{

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            val newToken = refreshToken()
            Log.d("tokenvalue", "authenticate: ${newToken.body()}")
            newToken.body()?.let {
                sessionManager.saveBearerToken(it)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $it")
                    .build()
            }

        }
    }

    private suspend fun refreshToken() : retrofit2.Response<String> {
        return tokenApi.generateToken(CommonUtils.getEncodedAppId() , CommonUtils.CONTENT_TYPE)
    }

}