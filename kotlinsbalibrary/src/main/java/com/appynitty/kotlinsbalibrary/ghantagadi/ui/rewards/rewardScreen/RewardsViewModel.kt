package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.rewardScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Category
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DealsCategoriesResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.DealsCategoriesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(private val dealsCategoriesRepository: DealsCategoriesRepo) :
    ViewModel() {
    private val rewardsActivityChannel = Channel<RewardsActivityEvents>()
    val rewardsActivityEvent = rewardsActivityChannel.receiveAsFlow()

    private val _categoriesFlow = MutableStateFlow<List<Category>?>(null)
    val categoriesFlow: StateFlow<List<Category>?> = _categoriesFlow

    fun getDealsCategories() = viewModelScope.launch {
        if (_categoriesFlow.value != null && _categoriesFlow.value!!.isNotEmpty()) {
            return@launch
        }

        rewardsActivityChannel.send(RewardsActivityEvents.ShowProgressBar)
        try {
            val response = dealsCategoriesRepository.getDealsCategories()
            handleDealsCategoriesResponse(response)
        } catch (t: Throwable) {
            rewardsActivityChannel.send(RewardsActivityEvents.HideProgressBar)
            when (t) {
                is IOException -> rewardsActivityChannel.send(RewardsActivityEvents.OnFailure("Connection Timeout"))
                else -> rewardsActivityChannel.send(RewardsActivityEvents.OnFailure("Conversion Error"))
            }
        }

    }

    private fun handleDealsCategoriesResponse(response: Response<DealsCategoriesResponse>) {

        viewModelScope.launch {
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                when (responseBody.status) {
                    CommonUtils.STATUS_SUCCESS_CAPS -> {
                        _categoriesFlow.value = responseBody.categoryList
                        rewardsActivityChannel.send(RewardsActivityEvents.OnSuccess(responseBody))
                    }

                    CommonUtils.STATUS_ERROR_CAPE -> {
                        rewardsActivityChannel.send(RewardsActivityEvents.OnFailure(responseBody.message))
                    }
                }
            } else {
                rewardsActivityChannel.send(RewardsActivityEvents.OnFailure(response.message()))
            }
            rewardsActivityChannel.send(RewardsActivityEvents.HideProgressBar)
        }
    }
}

sealed class RewardsActivityEvents {
    object ShowProgressBar : RewardsActivityEvents()
    object HideProgressBar : RewardsActivityEvents()
    data class OnSuccess(val response: DealsCategoriesResponse) : RewardsActivityEvents()
    data class OnFailure(val message: String) : RewardsActivityEvents()
}