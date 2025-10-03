package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.WorkHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

private const val TAG = "WorkHistoryViewModel"

@HiltViewModel
class WorkHistoryViewModel @Inject constructor(
    private val workHistoryRepository: WorkHistoryRepository,
) : ViewModel() {

    val workHistoryResponseResponseLiveData: MutableLiveData<ApiResponseListener<List<WorkHistoryResponse>>> =
        MutableLiveData()


    fun getWorkHistoryList(
        appId: String,
        userId: String,
        year: String,
        month: String,
        empType: String
    ) = viewModelScope.launch {

        workHistoryResponseResponseLiveData.postValue(ApiResponseListener.Loading())
        try {
            val response =
                workHistoryRepository.getWorkHistoryList(appId, userId, year, month, empType)
            workHistoryResponseResponseLiveData.postValue(handleWorkHistoryResponse(response))

        } catch (t: Throwable) {
            when (t) {
                is IOException -> workHistoryResponseResponseLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Connection Timeout"
                    )
                )
                else -> workHistoryResponseResponseLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }
    }

    private fun handleWorkHistoryResponse(response: Response<List<WorkHistoryResponse>>): ApiResponseListener<List<WorkHistoryResponse>> {
        if (response.isSuccessful) {
            response.body()?.let {
                return ApiResponseListener.Success(it)
            }
        }
        return ApiResponseListener.Failure(response.message())
    }


}

