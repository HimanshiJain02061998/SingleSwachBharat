package com.appynitty.kotlinsbalibrary.common.ui.addCity.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.model.response.DistrictListItem
import com.appynitty.kotlinsbalibrary.common.model.response.GetDistrictListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.GetUlbListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.ULBListItem
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddCityViewModel @Inject constructor(
    private val getUlbDetailsRepository: GetUlbDetailsRepository
) : ViewModel() {

    private val addCityEventChannel = Channel<AddCityEvent>()
    val addCityEventsFlow = addCityEventChannel.receiveAsFlow()

    fun getDistrictList(
    ) = viewModelScope.launch {

        addCityEventChannel.send(AddCityEvent.ShowProgressBar)
        try {

            val response = getUlbDetailsRepository.getDistrictList()
            getDistrictListResponse(response)

        } catch (t: Throwable) {
            addCityEventChannel.send(AddCityEvent.HideProgressBar)
            when (t) {
                is IOException -> addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Connection Timeout"))
                else -> addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Conversion Error"))
            }

        }

    }

    private fun getDistrictListResponse(
        response: Response<GetDistrictListResponse>,
    ) =
        viewModelScope.launch {
            addCityEventChannel.send(AddCityEvent.HideProgressBar)
            if (response.isSuccessful) {

                if (response.body() != null) {
                    val it = response.body()

                    if (it?.status == CommonUtils.GIS_STATUS_SUCCESS) {

                        addCityEventChannel.send(
                            AddCityEvent.DistrictListResponse(
                                it.districtList
                            )
                        )
                        Log.d("districtlist","list is viewmodel ${it.districtList}")


                    } else if (it?.status == CommonUtils.STATUS_ERROR) {
                        addCityEventChannel.send(
                            AddCityEvent.ShowResponseErrorMessage(
                                it.message,
                                it.messageMar
                            )
                        )
                    }
                }
            }
        }

    fun getUlbList(
        disId: Int
    ) = viewModelScope.launch {

        addCityEventChannel.send(AddCityEvent.ShowProgressBar)
        try {

            val response = getUlbDetailsRepository.getUlbList(disId)
            getUlbListResponse(response)

        } catch (t: Throwable) {
            addCityEventChannel.send(AddCityEvent.HideProgressBar)
            when (t) {
                is IOException -> addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Connection Timeout"))
                else -> addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Conversion Error"))
            }

        }

    }

    private fun getUlbListResponse(
        response: Response<GetUlbListResponse>,
    ) =
        viewModelScope.launch {
            addCityEventChannel.send(AddCityEvent.HideProgressBar)
            if (response.isSuccessful) {

                if (response.body() != null) {
                    val it = response.body()

                    if (it?.status == CommonUtils.GIS_STATUS_SUCCESS) {

                        addCityEventChannel.send(
                            AddCityEvent.UlbListResponse(
                                it.uLBList
                            )
                        )

                    } else if (it?.status == CommonUtils.STATUS_ERROR) {
                        addCityEventChannel.send(
                            AddCityEvent.ShowResponseErrorMessage(
                                it.message,
                                it.messageMar
                            )
                        )
                    }
                }
            }
        }

    fun validateUlb(dist: String,ulb: String): Boolean {
            return when {
                dist.isEmpty() || dist == "" -> {
                    viewModelScope.launch {
                        addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Please select your district"))
                    }
                    false
                }
                ulb.isEmpty() || ulb == "" -> {
                    viewModelScope.launch {
                        addCityEventChannel.send(AddCityEvent.ShowFailureMessage("Please select your ulb"))
                    }
                    false
                }
                else -> true
            }
        }


    sealed class AddCityEvent {
        object ShowProgressBar : AddCityEvent()
        object HideProgressBar : AddCityEvent()
        object NavigateToLogin : AddCityEvent()
        data class ShowFailureMessage(val msg: String) : AddCityEvent()
        data class UlbListResponse(val uLbList: List<ULBListItem?>?) : AddCityEvent()
        data class DistrictListResponse(val districtList: List<DistrictListItem?>?) : AddCityEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) : AddCityEvent()
        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : AddCityEvent()
    }

}


