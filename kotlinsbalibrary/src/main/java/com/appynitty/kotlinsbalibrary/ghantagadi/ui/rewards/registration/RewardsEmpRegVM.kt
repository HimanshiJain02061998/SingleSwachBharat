package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.registration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils.Companion.STATUS_ERROR_CAPE
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils.Companion.STATUS_SUCCESS_CAPS
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RewardsRegRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RewardsRegResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.RewardsWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RewardsEmpRegVM @Inject constructor(private val rewardsWalletRepository: RewardsWalletRepository) :
    ViewModel() {
    val empRegistrationLiveData: MutableLiveData<ApiResponseListener<RewardsRegResponse>> =
        MutableLiveData()

    fun registerEmployee(registrationRequest: RewardsRegRequest) = viewModelScope.launch {
        empRegistrationLiveData.postValue(ApiResponseListener.Loading())
        try {
            val response = rewardsWalletRepository.registerEmployee(registrationRequest)
            handleEmpRegistrationResult(response)
        } catch (t: Throwable) {
            when (t) {
                is IOException -> empRegistrationLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Network Failure"
                    )
                )

                else -> empRegistrationLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }
    }

    private fun handleEmpRegistrationResult(response: Response<RewardsRegResponse>) {
        if (response.isSuccessful) {
            response.body()?.let {
                when (it.status) {
                    STATUS_SUCCESS_CAPS -> {
                        empRegistrationLiveData.postValue(ApiResponseListener.Success(it))
                    }

                    STATUS_ERROR_CAPE -> {
                        empRegistrationLiveData.postValue(ApiResponseListener.Failure(it.message))
                    }
                }
            }
        } else {
            empRegistrationLiveData.postValue(ApiResponseListener.Failure(response.message()))
        }
    }
}