package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.transaction_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.TransactionHistoryResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.TransactionHistoryRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryVM @Inject constructor(private val transactionHistoryRepo: TransactionHistoryRepo) :
    ViewModel() {
    private val _transactionHistoryState =
        MutableStateFlow<TransactionHistoryState>(TransactionHistoryState.Loading)
    val transactionHistoryState: StateFlow<TransactionHistoryState> = _transactionHistoryState

    fun getTransactionHistory(walletId: String, transactionDate: String) {

        viewModelScope.launch {
            _transactionHistoryState.value = TransactionHistoryState.Loading
            try {
                val response =
                    transactionHistoryRepo.getTransactionHistory(walletId, transactionDate)
                handleResponse(response)
            } catch (t: Throwable) {
                _transactionHistoryState.value = TransactionHistoryState.Error("Network Error")
            }
        }
    }

    private fun handleResponse(response: Response<TransactionHistoryResponse>) {
        val responseBody = response.body()
        if (response.isSuccessful && responseBody != null) {
            when (responseBody.status) {
                CommonUtils.STATUS_SUCCESS_CAPS -> {
                    _transactionHistoryState.value = TransactionHistoryState.Success(responseBody)
                }

                CommonUtils.STATUS_ERROR_CAPE -> {
                    _transactionHistoryState.value =
                        TransactionHistoryState.Error(responseBody.message)
                }
            }
        } else {
            _transactionHistoryState.value = TransactionHistoryState.Error(response.message())
        }

    }
}

sealed class TransactionHistoryState {
    object Loading : TransactionHistoryState()
    data class Success(val data: TransactionHistoryResponse) : TransactionHistoryState()
    data class Error(val message: String) : TransactionHistoryState()
}