package com.appynitty.kotlinsbalibrary.common.ui.addUlb.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddUlbViewModel @Inject constructor(
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val addUlbEventChannel = Channel<AddUlbEvent>()
    val addUlbEventsFlow = addUlbEventChannel.receiveAsFlow()


    fun validateUlb(dist: String,ulb: String): Boolean {
        return when {
            dist.isEmpty() || dist == "" -> {
                viewModelScope.launch {
                    addUlbEventChannel.send(AddUlbEvent.ShowFailureMessageRes(R.string.please_select_a_district_first))
                }
                false
            }
            ulb.isEmpty() || ulb == "" -> {
                viewModelScope.launch {
                    addUlbEventChannel.send(AddUlbEvent.ShowFailureMessageRes(R.string.please_select_a_ulb_before_continuing))
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

            addUlbEventChannel.send(AddUlbEvent.ShowSuccessMessage(R.string.ulb_selected_successfully))
            addUlbEventChannel.send(
                AddUlbEvent.NavigateToLogin
            )
        }
    }

    sealed class AddUlbEvent {
        object NavigateToLogin : AddUlbEvent()
        data class ShowFailureMessage(val msg: String) : AddUlbEvent()
        data class ShowFailureMessageRes(val resourceId: Int) : AddUlbEvent()
        data class ShowSuccessMessage(val resourceId: Int) : AddUlbEvent()
    }

}

