package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AboutCoinsResponse
import retrofit2.Response
import retrofit2.http.GET

interface AboutCoinsWebService {
    @GET("api/Citizen/AboutCoinsDetails")
    suspend fun getAboutCoins(): Response<AboutCoinsResponse>
}