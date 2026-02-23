package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Voucher
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.VouchersRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VouchersViewModel @Inject constructor(
    private val vouchersRepo: VouchersRepo,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _activeVouchersState = MutableStateFlow<VouchersState>(VouchersState.Loading)
    val activeVouchersState: StateFlow<VouchersState> = _activeVouchersState

    private val _claimedVouchersState = MutableStateFlow<VouchersState>(VouchersState.Loading)
    val claimedVouchersState: StateFlow<VouchersState> = _claimedVouchersState

    private val _expiredVouchersState = MutableStateFlow<VouchersState>(VouchersState.Loading)
    val expiredVouchersState: StateFlow<VouchersState> = _expiredVouchersState

    var isActiveVouchersFetched: Boolean = savedStateHandle["isActiveVouchersFetched"] ?: false
        set(value) {
            field = value
            savedStateHandle["isActiveVouchersFetched"] = value
        }

    var isClaimedVouchersFetched: Boolean = savedStateHandle["isClaimedVouchersFetched"] ?: false
        set(value) {
            field = value
            savedStateHandle["isClaimedVouchersFetched"] = value
        }

    var isExpiredVouchersFetched: Boolean = savedStateHandle["isExpiredVouchersFetched"] ?: false
        set(value) {
            field = value
            savedStateHandle["isExpiredVouchersFetched"] = value
        }

    fun fetchActiveVouchers(walletId: String, date: String) {
        viewModelScope.launch {

            if (isActiveVouchersFetched) return@launch

            try {
                _activeVouchersState.value = VouchersState.Loading
                val response = vouchersRepo.getActiveVouchers(walletId, date)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    when (responseBody.status) {
                        CommonUtils.STATUS_SUCCESS_CAPS -> {
                            val activeVouchers = responseBody.voucherDetails
                            if (activeVouchers.isEmpty()) {
                                _activeVouchersState.value = VouchersState.Empty
                            } else {
                                _activeVouchersState.value = VouchersState.Success(activeVouchers)
                            }

                            isActiveVouchersFetched = true
                            savedStateHandle["activeVouchers"] = activeVouchers
                        }

                        CommonUtils.STATUS_ERROR_CAPE -> {
                            _activeVouchersState.value = VouchersState.Error(responseBody.message)
                        }
                    }
                } else {
                    _activeVouchersState.value = VouchersState.Error(response.message())
                }

            } catch (e: Exception) {
                _activeVouchersState.value = VouchersState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchClaimedVouchers(walletId: String, date: String) {
        viewModelScope.launch {
            if (isClaimedVouchersFetched) return@launch

            try {
                _claimedVouchersState.value = VouchersState.Loading
                val response = vouchersRepo.getClaimedVouchers(walletId, date)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    when (responseBody.status) {
                        CommonUtils.STATUS_SUCCESS_CAPS -> {
                            val claimedVouchers = responseBody.voucherDetails
                            if (claimedVouchers.isEmpty()) {
                                _claimedVouchersState.value = VouchersState.Empty
                            } else {
                                _claimedVouchersState.value = VouchersState.Success(claimedVouchers)
                            }

                            isClaimedVouchersFetched = true
                            savedStateHandle["claimedVouchers"] = claimedVouchers
                        }

                        CommonUtils.STATUS_ERROR_CAPE -> {
                            _claimedVouchersState.value = VouchersState.Error(responseBody.message)
                        }
                    }
                } else {
                    _claimedVouchersState.value = VouchersState.Error(response.message())
                }

            } catch (e: Exception) {
                _claimedVouchersState.value = VouchersState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchExpiredVouchers(walletId: String, date: String) {
        viewModelScope.launch {
            if (isExpiredVouchersFetched) return@launch
            _expiredVouchersState.value = VouchersState.Loading
            try {
                val response = vouchersRepo.getExpiredVouchers(walletId, date)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    when (responseBody.status) {
                        CommonUtils.STATUS_SUCCESS_CAPS -> {
                            val expiredVouchers = responseBody.voucherDetails
                            if (expiredVouchers.isEmpty()) {
                                _expiredVouchersState.value = VouchersState.Empty
                            } else {
                                _expiredVouchersState.value = VouchersState.Success(expiredVouchers)
                            }

                            isExpiredVouchersFetched = true
                            savedStateHandle["expiredVouchers"] = expiredVouchers
                        }

                        CommonUtils.STATUS_ERROR_CAPE -> {
                            _expiredVouchersState.value = VouchersState.Error(responseBody.message)
                        }
                    }
                } else {
                    _expiredVouchersState.value = VouchersState.Error(response.message())
                }

            } catch (e: Exception) {
                _expiredVouchersState.value = VouchersState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class VouchersState {
    object Loading : VouchersState()
    data class Success(val vouchers: List<Voucher>) : VouchersState()
    data class Error(val message: String) : VouchersState()
    object Empty : VouchersState()
}