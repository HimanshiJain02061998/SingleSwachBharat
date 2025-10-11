package com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.viewModel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SelectCommonViewmodel @Inject constructor(
    private val getUlbDetailsRepository: GetUlbDetailsRepository,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val selectCommonEventChannel = Channel<SelectCommonEvent>()
    val selectCommonEventsFlow = selectCommonEventChannel.receiveAsFlow()

    fun getDistrictList(
    ) = viewModelScope.launch {

        selectCommonEventChannel.send(SelectCommonEvent.ShowProgressBar)
        try {

            val response = getUlbDetailsRepository.getDistrictList()
            getDistrictListResponse(response)

        } catch (t: Throwable) {
            selectCommonEventChannel.send(SelectCommonEvent.HideProgressBar)
            when (t) {
                is IOException -> selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessage("Connection Timeout"))
                else -> selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessage("Conversion Error"))
            }

        }

    }

    private fun getDistrictListResponse(
        response: Response<GetDistrictListResponse>,
    ) =
        viewModelScope.launch {
            selectCommonEventChannel.send(SelectCommonEvent.HideProgressBar)
            if (response.isSuccessful) {

                if (response.body() != null) {
                    val it = response.body()

                    if (it?.status == CommonUtils.GIS_STATUS_SUCCESS) {

                        selectCommonEventChannel.send(
                            SelectCommonEvent.DistrictListResponse(
                                it.districtList
                            )
                        )
                        Log.d("districtlist","list is viewmodel ${it.districtList}")


                    } else if (it?.status == CommonUtils.STATUS_ERROR) {
                        selectCommonEventChannel.send(
                            SelectCommonEvent.ShowResponseErrorMessage(
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

        selectCommonEventChannel.send(SelectCommonEvent.ShowProgressBar)
        try {

            val response = getUlbDetailsRepository.getUlbList(disId)
            getUlbListResponse(response)

        } catch (t: Throwable) {
            selectCommonEventChannel.send(SelectCommonEvent.HideProgressBar)
            when (t) {
                is IOException -> selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessage("Connection Timeout"))
                else -> selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessage("Conversion Error"))
            }

        }

    }

    private fun getUlbListResponse(
        response: Response<GetUlbListResponse>,
    ) =
        viewModelScope.launch {
            selectCommonEventChannel.send(SelectCommonEvent.HideProgressBar)
            if (response.isSuccessful) {

                if (response.body() != null) {
                    val it = response.body()

                    if (it?.status == CommonUtils.GIS_STATUS_SUCCESS) {

                        selectCommonEventChannel.send(
                            SelectCommonEvent.UlbListResponse(
                                it.uLBList
                            )
                        )

                    } else if (it?.status == CommonUtils.STATUS_ERROR) {
                        selectCommonEventChannel.send(
                            SelectCommonEvent.ShowResponseErrorMessage(
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
                    selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessageRes(R.string.please_select_a_district_first))
                }
                false
            }
            ulb.isEmpty() || ulb == "" -> {
                viewModelScope.launch {
                    selectCommonEventChannel.send(SelectCommonEvent.ShowFailureMessageRes(R.string.please_select_a_ulb_before_continuing))
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

            selectCommonEventChannel.send(SelectCommonEvent.ShowSuccessMessage(R.string.ulb_selected_successfully))
            selectCommonEventChannel.send(
                SelectCommonEvent.NavigateToLogin
            )
        }
    }

    sealed class SelectCommonEvent {
        object ShowProgressBar : SelectCommonEvent()
        object HideProgressBar : SelectCommonEvent()
        object NavigateToLogin : SelectCommonEvent()
        data class ShowFailureMessage(val msg: String) : SelectCommonEvent()
        data class ShowFailureMessageRes(val resourceId: Int) : SelectCommonEvent()
        data class UlbListResponse(val uLbList: List<ULBListItem?>?) : SelectCommonEvent()
        data class DistrictListResponse(val districtList: List<DistrictListItem?>?) : SelectCommonEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) : SelectCommonEvent()
        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : SelectCommonEvent()
        data class ShowSuccessMessage(val resourceId: Int) : SelectCommonEvent()
    }

}