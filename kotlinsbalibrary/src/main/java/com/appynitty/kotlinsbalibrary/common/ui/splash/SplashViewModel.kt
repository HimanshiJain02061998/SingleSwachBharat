package com.appynitty.kotlinsbalibrary.common.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.model.response.ForceUpdateResponse
import com.appynitty.kotlinsbalibrary.common.repository.UpdateRepository
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userDataStore: UserDataStore,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val splashChannelEvent = Channel<SplashEvent>()
    val splashEventsFlow = splashChannelEvent.receiveAsFlow()

    fun checkWhereToNavigate() = viewModelScope.launch {

        //getting isUserLogged in value from datastore
        val isUserLoggedIn = sessionDataStore.getIsUserLoggedIn.first()

        if (isUserLoggedIn) {
            val userData = userDataStore.getUserEssentials.first()
            val userType = userData.userTypeId

            if (userType == "0") {
                // ghanta gadi user
                splashChannelEvent.send(SplashEvent.NavigateToDashboardScreen)
            } else {
                // house scanify user
                splashChannelEvent.send(SplashEvent.NavigateToEmpDashBoardScreen)
            }
        } else {
            // if user is not logged in send him to login page
            splashChannelEvent.send(SplashEvent.NavigateToLoginScreen)
        }

    }

    sealed class SplashEvent {
        object NavigateToLoginScreen : SplashEvent()
        object NavigateToDashboardScreen : SplashEvent()
        object NavigateToEmpDashBoardScreen : SplashEvent()
        data class ShowErrorMsg(val msg: String) : SplashEvent()

    }
}