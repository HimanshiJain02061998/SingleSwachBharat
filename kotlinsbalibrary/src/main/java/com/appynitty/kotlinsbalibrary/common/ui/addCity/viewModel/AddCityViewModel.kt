package com.appynitty.kotlinsbalibrary.common.ui.addCity.viewModel

import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.model.response.DistrictListItem
import com.appynitty.kotlinsbalibrary.common.model.response.GetDistrictListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.GetUlbListResponse
import com.appynitty.kotlinsbalibrary.common.model.response.ULBListItem
import com.appynitty.kotlinsbalibrary.common.repository.GetUlbDetailsRepository
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardViewModel.DashboardEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddCityViewModel @Inject constructor(
    private val getUlbDetailsRepository: GetUlbDetailsRepository,
    private val userDataStore: UserDataStore
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
                        addCityEventChannel.send(AddCityEvent.ShowFailureMessageRes(R.string.please_select_a_district_first))
                    }
                    false
                }
                ulb.isEmpty() || ulb == "" -> {
                    viewModelScope.launch {
                        addCityEventChannel.send(AddCityEvent.ShowFailureMessageRes(R.string.please_select_a_ulb_before_continuing))
                    }
                    false
                }
                else -> true
            }
        }

    fun selectUlb(selectedAppId: String?,ulbName: String?){
        viewModelScope.launch {
            selectedAppId?.let {
                userDataStore.saveAppId(it)
                ulbName?.let { it1 -> userDataStore.saveUlbName(it1) }
                MyApplication.APP_ID = selectedAppId
                ulbName?.let { ulb -> MyApplication.ULB_NAME = ulb  }
            }

            addCityEventChannel.send(AddCityEvent.ShowSuccessMessage(R.string.ulb_selected_successfully))
            addCityEventChannel.send(
                AddCityEvent.NavigateToLogin
            )
        }
    }

    sealed class AddCityEvent {
        object ShowProgressBar : AddCityEvent()
        object HideProgressBar : AddCityEvent()
        object NavigateToLogin : AddCityEvent()
        data class ShowFailureMessage(val msg: String) : AddCityEvent()
        data class ShowFailureMessageRes(val resourceId: Int) : AddCityEvent()
        data class UlbListResponse(val uLbList: List<ULBListItem?>?) : AddCityEvent()
        data class DistrictListResponse(val districtList: List<DistrictListItem?>?) : AddCityEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) : AddCityEvent()
        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : AddCityEvent()
        data class ShowSuccessMessage(val resourceId: Int) : AddCityEvent()
    }

}


