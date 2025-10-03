package com.appynitty.kotlinsbalibrary.housescanify.ui.empHistory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpWorkHistoryResponse
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpWorkHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EmpWorkHistoryViewModel @Inject constructor(
    private val empWorkHistoryRepository: EmpWorkHistoryRepository
) : ViewModel() {

    val workHistoryResponseLiveData: MutableLiveData<ApiResponseListener<List<EmpWorkHistoryResponse>>> =
        MutableLiveData()

    fun getWorkHistoryList(
        appId: String,
        contentType: String,
        userId: String,
        year: String,
        month: String
    ) = viewModelScope.launch {

        workHistoryResponseLiveData.postValue(ApiResponseListener.Loading())

        try {
            val response =
                empWorkHistoryRepository.getWorkHistoryList(appId, contentType, userId, year, month)
            workHistoryResponseLiveData.postValue(handleWorkHistoryResponse(response))

        } catch (t: Throwable) {
            when (t) {
                is IOException -> workHistoryResponseLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Connection Timeout"
                    )
                )
                else -> workHistoryResponseLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }
    }

    private fun handleWorkHistoryResponse(response: Response<List<EmpWorkHistoryResponse>>): ApiResponseListener<List<EmpWorkHistoryResponse>> {
        if (response.isSuccessful) {
            response.body()?.let {
                return ApiResponseListener.Success(it)
            }
        }
        return ApiResponseListener.Failure(response.message())
    }

}