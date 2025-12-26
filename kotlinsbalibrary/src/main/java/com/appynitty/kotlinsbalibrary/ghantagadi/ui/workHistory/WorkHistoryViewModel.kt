package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.WorkHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

private const val TAG = "WorkHistoryViewModel"

@HiltViewModel
class WorkHistoryViewModel @Inject constructor(
    private val workHistoryRepository: WorkHistoryRepository,
    private val userDataStore: UserDataStore,
    private val insertWorkHistoryDataUseCases: InsertWorkHistoryDataUseCases,
    private val workHistoryDao: WorkHistoryDao
) : ViewModel() {

    val workHistoryResponseResponseLiveData: MutableLiveData<ApiResponseListener<List<WorkHistoryResponse>>> =
        MutableLiveData()

    private val _isOfflineUi = MutableLiveData(false)
    val isOfflineUi: LiveData<Boolean> get() = _isOfflineUi

    private val _workHistoryLiveData =
        MutableLiveData<List<WorkHistoryResponse>>()

    val workHistoryLiveData: LiveData<List<WorkHistoryResponse>> =
        _workHistoryLiveData

    init {
        loadOfflineModeOnce()
    }


    fun getWorkHistoryDetailList(year: String,
                                 month: String,) {
        viewModelScope.launch {
            workHistoryDao.getWorkHistoryByMonthYear(year,month)
                .collect { list ->
                    _workHistoryLiveData.postValue(
                        list.map { it.toResponse() }
                    )
                }
        }
    }


    fun WorkHistoryData.toResponse() = WorkHistoryResponse(
        date = date,
        houseCollection = houseCollection,
        LiquidCollection = LiquidCollection,
        StreetCollection = StreetCollection,
        DumpYardCollection = DumpYardCollection,
        DumpYardPlantCollection = DumpYardPlantCollection
    )
    fun loadOfflineModeOnce() {
        viewModelScope.launch {
            val value = userDataStore.getIsOfflineMode.first()
            _isOfflineUi.value = value
            Log.d("checkStatus", "status is $value")
        }
    }
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
//            workHistoryResponseResponseLiveData.postValue(handleWorkHistoryResponse(response))
            insertWorkHistoryDataUseCases.invoke(response,year,month)

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

val dbList = listOf(  GarbageData(1,2,3,4,0),
    GarbageData(2,0,3,5,0),
    GarbageData(3,3,3,6,0))

val apiresponseList = listOf(  GarbageData(1,4,3,4,6),
    GarbageData(2,0,3,5,0),
    GarbageData(3,3,3,6,0),
    GarbageData(4,3,3,6,0),
            GarbageData(5,3,3,6,0)
    )

data class GarbageData(val id:Int,val streetCount:Int,val houseCount:Int,val liquidCount:Int,val masterPlateCount:Int)
