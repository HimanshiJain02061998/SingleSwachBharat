package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface GetOffersDetailsWebService {
    @GET("/api/Citizen/GetOfferDetailsByOfferId")
    suspend fun getOfferDetailsByOfferId(
        @Header("WalletId") walletId: String,
        @Header("offerId") offerId: Int
    ): Response<OfferDetails>
}