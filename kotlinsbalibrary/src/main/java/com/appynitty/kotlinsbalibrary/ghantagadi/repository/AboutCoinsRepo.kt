package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.AboutCoinsWebService
import javax.inject.Inject

class AboutCoinsRepo @Inject constructor(private val aboutCoinsWebService: AboutCoinsWebService) {

    suspend fun getAboutCoinsDetails() = aboutCoinsWebService.getAboutCoins()
}