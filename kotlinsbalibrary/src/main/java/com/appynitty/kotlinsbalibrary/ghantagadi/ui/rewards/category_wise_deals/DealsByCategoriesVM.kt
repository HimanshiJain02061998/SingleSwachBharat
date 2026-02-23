package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.category_wise_deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetailsCatWiseResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.DealsCategoriesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DealsByCategoriesVM @Inject constructor(private val dealsCategoriesRepo: DealsCategoriesRepo) :
    ViewModel() {
    private val _dealsByCategoriesFlow =
        MutableStateFlow<DealsByCategoriesEvents>(DealsByCategoriesEvents.Idle)

    val dealsByCategoriesFlow: StateFlow<DealsByCategoriesEvents> =
        _dealsByCategoriesFlow.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow(-1)
    val selectedCategoryId: StateFlow<Int> = _selectedCategoryId.asStateFlow()

    private val categoryDataCache = mutableMapOf<Int, OfferDetailsCatWiseResponse>()

    fun getDealsCategoryWise(categoryId: Int, walletId: String) =
        viewModelScope.launch {

            val cachedData = categoryDataCache[categoryId]
            if (cachedData != null) {
                _dealsByCategoriesFlow.value = DealsByCategoriesEvents.Success(cachedData)
                return@launch
            }

            _dealsByCategoriesFlow.value = DealsByCategoriesEvents.Loading
            try {
                val response = dealsCategoriesRepo.getDealsCatWise(categoryId, walletId)
                handleDealsByCategoriesResponse(categoryId, response)
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> _dealsByCategoriesFlow.value =
                        DealsByCategoriesEvents.Error("Network Failure")

                    else -> _dealsByCategoriesFlow.value =
                        DealsByCategoriesEvents.Error("Conversion Error")
                }
            }
        }

    private fun handleDealsByCategoriesResponse(
        categoryId: Int,
        response: Response<OfferDetailsCatWiseResponse>
    ) =
        viewModelScope.launch {
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                when (responseBody.status) {
                    CommonUtils.STATUS_SUCCESS_CAPS -> {
                        categoryDataCache[categoryId] = responseBody
                        _dealsByCategoriesFlow.value = DealsByCategoriesEvents.Success(responseBody)
                    }

                    CommonUtils.STATUS_ERROR_CAPE -> {
                        _dealsByCategoriesFlow.value =
                            DealsByCategoriesEvents.Error(responseBody.message)
                    }
                }
            } else {
                _dealsByCategoriesFlow.value = DealsByCategoriesEvents.Error(response.message())
            }

        }

}

sealed class DealsByCategoriesEvents {
    object Idle : DealsByCategoriesEvents()
    object Loading : DealsByCategoriesEvents()
    data class Error(val message: String) : DealsByCategoriesEvents()
    data class Success(val response: OfferDetailsCatWiseResponse) : DealsByCategoriesEvents()
}