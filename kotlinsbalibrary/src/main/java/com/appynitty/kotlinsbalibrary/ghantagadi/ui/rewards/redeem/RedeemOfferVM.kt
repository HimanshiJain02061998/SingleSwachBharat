package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.redeem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RedeemOfferRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetails
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RedeemedOfferResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.RedeemOfferRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RedeemOfferVM @Inject constructor(private val redeemOfferRepo: RedeemOfferRepo) :
    ViewModel() {
    private val redeemOfferEventsChannel = Channel<RedeemActivityEvents>()
    val redeemOfferEventsFlow = redeemOfferEventsChannel.receiveAsFlow()

    private val _offerDetails = MutableStateFlow<OfferDetails?>(null)

    fun getOfferDetails(walletId: String, offerId: Int) = viewModelScope.launch {
        if (_offerDetails.value != null) {
            redeemOfferEventsChannel.send(RedeemActivityEvents.OnSuccess(_offerDetails.value))
            return@launch
        }

        redeemOfferEventsChannel.send(RedeemActivityEvents.ShowProgressBar)
        try {
            val response = redeemOfferRepo.getOfferDetailsByOfferId(walletId, offerId)
            handleOfferDetailsResponse(response)
        } catch (t: Throwable) {
            Timber.e("getOfferDetails: ${t.message}")
            redeemOfferEventsChannel.send(RedeemActivityEvents.HideProgressBar)
            when (t) {
                is IOException ->
                    redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage("Connection Timeout"))

                else -> redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage("Conversion Error"))
            }
        }
    }

    fun redeemOffer(reqBody: RedeemOfferRequest) = viewModelScope.launch {
        redeemOfferEventsChannel.send(RedeemActivityEvents.ShowProgressBar)
        try {
            val response = redeemOfferRepo.redeemOffer(reqBody)
            handleRedeemOfferResponse(response)
        } catch (t: Throwable) {
            Timber.e("redeemOffer: ${t.message}")
            redeemOfferEventsChannel.send(RedeemActivityEvents.HideProgressBar)
            when (t) {
                is IOException ->
                    redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage("Connection Timeout"))

                else -> redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage("Conversion Error"))
            }
        }
    }

    private fun handleRedeemOfferResponse(response: Response<RedeemedOfferResponse>) {
        viewModelScope.launch {
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()
                when (responseBody?.status) {
                    CommonUtils.STATUS_SUCCESS_CAPS -> {
                        redeemOfferEventsChannel.send(RedeemActivityEvents.OnSuccess(responseBody))
                    }

                    CommonUtils.STATUS_ERROR_CAPE -> {
                        redeemOfferEventsChannel.send(
                            RedeemActivityEvents.ShowFailureMessage(
                                responseBody.message
                            )
                        )
                    }
                }
            } else {
                redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage(response.message()))
            }
            redeemOfferEventsChannel.send(RedeemActivityEvents.HideProgressBar)
        }
    }

    private fun handleOfferDetailsResponse(response: Response<OfferDetails>) {
        viewModelScope.launch {
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()
                when (responseBody?.status) {
                    CommonUtils.STATUS_SUCCESS_CAPS -> {
                        _offerDetails.value = responseBody
                        redeemOfferEventsChannel.send(RedeemActivityEvents.OnSuccess(responseBody))
                    }

                    CommonUtils.STATUS_ERROR_CAPE -> {
                        redeemOfferEventsChannel.send(
                            RedeemActivityEvents.ShowFailureMessage(
                                responseBody.message
                            )
                        )
                    }
                }
            } else {
                redeemOfferEventsChannel.send(RedeemActivityEvents.ShowFailureMessage(response.message()))
            }
            redeemOfferEventsChannel.send(RedeemActivityEvents.HideProgressBar)
        }
    }
}

sealed class RedeemActivityEvents {
    object ShowProgressBar : RedeemActivityEvents()
    object HideProgressBar : RedeemActivityEvents()
    data class OnSuccess(val data: Any?) : RedeemActivityEvents()
    data class ShowFailureMessage(val msg: String) : RedeemActivityEvents()
}