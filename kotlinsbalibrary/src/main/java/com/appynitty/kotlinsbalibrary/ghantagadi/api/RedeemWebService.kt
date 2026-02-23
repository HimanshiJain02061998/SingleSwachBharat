package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RedeemOfferRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RedeemedOfferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RedeemWebService {
    @POST("/api/Employee/EmployeeRedeemOffer")
    suspend fun redeemOffer(
        @Body redeemOfferBody: RedeemOfferRequest
    ): Response<RedeemedOfferResponse>
}