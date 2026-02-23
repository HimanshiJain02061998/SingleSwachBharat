package com.appynitty.kotlinsbalibrary.ghantagadi.api

import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DealsCategoriesResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetailsCatWiseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface DealsCategoriesWebService {

    @GET("/api/Account/GetCatagoryList")
    suspend fun getDealsCategories(): Response<DealsCategoriesResponse>

    @GET("/api/Citizen/GetOfferDetailsCatWise")
    suspend fun getDealsCatWise(
        @Header("catId") categoryId: Int,
        @Header("WalletId") walletId: String
    ): Response<OfferDetailsCatWiseResponse>
}