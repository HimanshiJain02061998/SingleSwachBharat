package com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.WorkHistoryRepository
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpWorkHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WorkHistoryDetailViewModel @Inject constructor(
    private val workHistoryRepository: WorkHistoryRepository,
    private val empWorkHistoryRepository: EmpWorkHistoryRepository
) : ViewModel() {

    val workHistoryDetailsResponseLiveData: MutableLiveData<ApiResponseListener<List<WorkHistoryDetailsResponse>>> =
        MutableLiveData()

    val empWorkHistoryDetailsResponseLiveData: MutableLiveData<ApiResponseListener<List<EmpHistoryDetailsResponse>>> =
        MutableLiveData()


    fun getWorkHistoryDetailList(
        appId: String,
        userId: String,
        fDate: String,
        languageId: String
    ) = viewModelScope.launch {

        workHistoryDetailsResponseLiveData.postValue(ApiResponseListener.Loading())

        try {
            val response =
                workHistoryRepository.getWorkHistoryDetailList(appId, userId, fDate, languageId)
            workHistoryDetailsResponseLiveData.postValue(handleWorkHistoryDetailsResponse(response))

        } catch (t: Throwable) {
            when (t) {
                is IOException -> workHistoryDetailsResponseLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Connection timeout"
                    )
                )
                else -> workHistoryDetailsResponseLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }
    }

    fun getEmpWorkHistoryDetailList(
        appId: String,
        contentType: String,
        userId: String,
        fDate: String,
    ) = viewModelScope.launch {

        empWorkHistoryDetailsResponseLiveData.postValue(ApiResponseListener.Loading())

        try {
            val response =
                empWorkHistoryRepository.getWorkHistoryDetailList(appId, contentType, userId, fDate)
            empWorkHistoryDetailsResponseLiveData.postValue(
                handleEmpWorkHistoryDetailsResponse(
                    response
                )
            )

        } catch (t: Throwable) {
            when (t) {
                is IOException -> empWorkHistoryDetailsResponseLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Connection timeout"
                    )
                )
                else -> empWorkHistoryDetailsResponseLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Conversion Error"
                    )
                )
            }
        }
    }

    private fun handleWorkHistoryDetailsResponse(response: Response<List<WorkHistoryDetailsResponse>>): ApiResponseListener<List<WorkHistoryDetailsResponse>> {
        if (response.isSuccessful) {
            response.body()?.let {
                return ApiResponseListener.Success(it)
            }
        }
        return ApiResponseListener.Failure(response.message())
    }


    private fun handleEmpWorkHistoryDetailsResponse(response: Response<List<EmpHistoryDetailsResponse>>): ApiResponseListener<List<EmpHistoryDetailsResponse>> {
        if (response.isSuccessful) {
            response.body()?.let {
                return ApiResponseListener.Success(it)
            }
        }
        return ApiResponseListener.Failure(response.message())
    }
}