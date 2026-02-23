package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.about_coins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AboutCoinDetail
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AboutCoinsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.AboutCoinsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AboutCoinsViewModel @Inject constructor(private val aboutCoinsRepo: AboutCoinsRepo) :
    ViewModel()  {
    private val _aboutCoinsState = MutableStateFlow<AboutCoinsState>(AboutCoinsState.Loading(false))
    val aboutCoinsState: StateFlow<AboutCoinsState> = _aboutCoinsState
    private val _aboutCoinsList = MutableStateFlow<List<AboutCoinDetail>>(emptyList())
    val aboutCoinsList: StateFlow<List<AboutCoinDetail>> = _aboutCoinsList

    init {
        getAboutCoins()
    }

    private fun getAboutCoins() {
        viewModelScope.launch {
            _aboutCoinsState.value = AboutCoinsState.Loading(true)
            try {
                val response = aboutCoinsRepo.getAboutCoinsDetails()
                val responseBody = response.body()
                _aboutCoinsState.value = if (response.isSuccessful && responseBody != null) {
                    when (responseBody.status) {
                        CommonUtils.STATUS_SUCCESS_CAPS -> {
                            _aboutCoinsList.value = responseBody.aboutCoinDetails
                            AboutCoinsState.Success(responseBody)
                        }

                        CommonUtils.STATUS_ERROR_CAPE -> AboutCoinsState.Error(responseBody.message)
                        else -> AboutCoinsState.Error("Unknown error")
                    }
                } else {
                    AboutCoinsState.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (t: Throwable) {
                _aboutCoinsState.value = when (t) {
                    is IOException -> AboutCoinsState.Error("Connection Timeout")
                    else -> AboutCoinsState.Error("Conversion Error")
                }
            } finally {
                _aboutCoinsState.value = AboutCoinsState.Loading(false)
            }
        }
    }
}

sealed class AboutCoinsState {
    data class Loading(val isLoading: Boolean) : AboutCoinsState()
    data class Success(val aboutCoinsResponse: AboutCoinsResponse) : AboutCoinsState()
    data class Error(val message: String) : AboutCoinsState()
}