package com.appynitty.kotlinsbalibrary.ghantagadi.repository

import com.appynitty.kotlinsbalibrary.ghantagadi.api.GetOffersDetailsWebService
import com.appynitty.kotlinsbalibrary.ghantagadi.api.RedeemWebService
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RedeemOfferRequest
import javax.inject.Inject

class RedeemOfferRepo @Inject constructor(
    private val offerDetailsWebService: GetOffersDetailsWebService,
    private val redeemWebService: RedeemWebService
) {
    suspend fun getOfferDetailsByOfferId(walletId: String, offerId: Int) =
        offerDetailsWebService.getOfferDetailsByOfferId(walletId, offerId)

    suspend fun redeemOffer(redeemOfferBody: RedeemOfferRequest) =
        redeemWebService.redeemOffer(redeemOfferBody)
}