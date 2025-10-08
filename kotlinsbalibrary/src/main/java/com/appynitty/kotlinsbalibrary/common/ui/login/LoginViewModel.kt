package com.appynitty.kotlinsbalibrary.common.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.dao.UserDetailsDao
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import com.appynitty.kotlinsbalibrary.common.model.response.LoginResponse
import com.appynitty.kotlinsbalibrary.common.model.response.UserDetailsResponse
import com.appynitty.kotlinsbalibrary.common.repository.LoginRepository
import com.appynitty.kotlinsbalibrary.common.repository.UserDetailsRepository
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserEssentials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 *  CREATED BY SANATH GOSAVI
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userDataStore: UserDataStore,
    private val sessionDataStore: SessionDataStore,
    private val userDetailsRepository: UserDetailsRepository,
    private val userDetailsDao: UserDetailsDao
) : ViewModel() {

    private val loginEventChannel = Channel<LoginEvent>()
    val loginEventsFlow = loginEventChannel.receiveAsFlow()

    fun saveLoginDetails(
        appId: String,
        contentType: String,
        loginRequest: LoginRequest
    ) = viewModelScope.launch {

        loginEventChannel.send(LoginEvent.ShowProgressBar)
        loginEventChannel.send(LoginEvent.DisableLoginButton)
        try {

            val response = loginRepository.saveLoginDetails(appId, contentType, loginRequest)
            handleLoginResponse(response, appId, contentType)

        } catch (t: Throwable) {
            loginEventChannel.send(LoginEvent.HideProgressBar)
            loginEventChannel.send(LoginEvent.EnableLoginButton)
            when (t) {
                is IOException -> loginEventChannel.send(LoginEvent.ShowFailureMessage("Connection Timeout"))
                else -> loginEventChannel.send(LoginEvent.ShowFailureMessage("Conversion Error"))
            }

        }

    }

    private fun handleLoginResponse(
        response: Response<LoginResponse>,
        appId: String,
        contentType: String
    ) =
        viewModelScope.launch {

            if (response.isSuccessful) {

                if (response.body() != null) {
                    val it = response.body()

                    if (it?.status == CommonUtils.STATUS_SUCCESS) {

                        saveUserLoginSession()
                        val userEssentials = UserEssentials(it.userId, it.EmpType, it.typeId)
                        userDataStore.saveUserEssentials(userEssentials)
                        getUserDetails(appId, contentType, it)

                        loginEventChannel.send(
                            LoginEvent.ShowResponseSuccessMessage(
                                it.message,
                                it.messageMar
                            )
                        )

                    } else if (it?.status == CommonUtils.STATUS_ERROR) {
                        loginEventChannel.send(LoginEvent.EnableLoginButton)
                        loginEventChannel.send(LoginEvent.HideProgressBar)
                        loginEventChannel.send(
                            LoginEvent.ShowResponseErrorMessage(
                                it.message,
                                it.messageMar
                            )
                        )
                    }else{
                        loginEventChannel.send(LoginEvent.HideProgressBar)

                    }
                }
            } else {
                loginEventChannel.send(LoginEvent.HideProgressBar)
                loginEventChannel.send(LoginEvent.EnableLoginButton)

            }
            loginEventChannel.send(LoginEvent.EnableLoginButton)
        }

    private fun getUserDetails(
        appId: String, content_type: String, loginResponse: LoginResponse
    ) = viewModelScope.launch {

        try {
            val response = userDetailsRepository.getUserDetails(
                appId,
                content_type,
                loginResponse.userId,
                loginResponse.typeId
            )
            handleUserDetailResult(response, loginResponse)

        } catch (t: Throwable) {
            loginEventChannel.send(LoginEvent.HideProgressBar)
            loginEventChannel.send(LoginEvent.EnableLoginButton)
            when (t) {
                is IOException -> loginEventChannel.send(LoginEvent.ShowFailureMessage("Connection Timeout"))
                else -> loginEventChannel.send(LoginEvent.ShowFailureMessage("Conversion Error"))
            }
        }
    }

    private fun handleUserDetailResult(
        response: Response<UserDetailsResponse>, loginResponse: LoginResponse
    ) = viewModelScope.launch {

        if (response.isSuccessful) {
            response.body()?.let {

                val userData = UserData(
                    loginResponse.userId,
                    loginResponse.typeId,
                    loginResponse.EmpType,
                    it.name,
                    it.nameMar,
                    it.employeeId,
                    it.profileImage,
                    it.address,
                    it.mobileNumber,
                    it.bloodGroup,
                    it.partnerName,
                    it.partnerCode
                )
                userDetailsDao.insertUser(userData)

                if (loginResponse.typeId == "0") {
                    loginEventChannel.send(LoginEvent.NavigateToDashboard)
                } else {
                    loginEventChannel.send(LoginEvent.NavigateToEmpDashboard)
                }
            }
        } else {
            loginEventChannel.send(LoginEvent.EnableLoginButton)
            loginEventChannel.send(LoginEvent.HideProgressBar)
            loginEventChannel.send(LoginEvent.ShowFailureMessage(response.message()))
        }

    }

    private fun saveUserLoginSession() = viewModelScope.launch {
        sessionDataStore.saveUserLoginSession(true)
    }


    sealed class LoginEvent {

        object ShowProgressBar : LoginEvent()
        object HideProgressBar : LoginEvent()
        object NavigateToDashboard : LoginEvent()
        object NavigateToEmpDashboard : LoginEvent()
        object EnableLoginButton : LoginEvent()
        object DisableLoginButton : LoginEvent()
        data class ShowFailureMessage(val msg: String) : LoginEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            LoginEvent()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) :
            LoginEvent()

    }

}