package com.appynitty.kotlinsbalibrary.common.ui.userDetails.viewmodel

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.dao.UserDetailsDao
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.model.response.UserDetailsResponse
import com.appynitty.kotlinsbalibrary.common.repository.UserDetailsRepository
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.Locale
import java.util.Locale.getDefault
import javax.inject.Inject

/**
 *  CREATED BY SANATH GOSAVI
 */

private const val TAG = "UserDetailsViewModel"

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val userDetailsRepository: UserDetailsRepository,
    private val userDetailsDao: UserDetailsDao
) : ViewModel() {

    val userDetailsLiveData: MutableLiveData<ApiResponseListener<UserDetailsResponse>> =
        MutableLiveData()


    fun getUserDetails(
        appId: String, content_type: String, userId: String, typeId: String, empType: String
    ) = viewModelScope.launch {

        userDetailsLiveData.postValue(ApiResponseListener.Loading())

        try {
            val response = userDetailsRepository.getUserDetails(appId, content_type, userId, typeId)
            userDetailsLiveData.postValue(handleUserDetailResult(response, userId, typeId, empType))

        } catch (t: Throwable) {
            when (t) {
                is IOException -> userDetailsLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Network Failure"
                    )
                )
                else -> userDetailsLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }


    }

    private fun handleUserDetailResult(
        response: Response<UserDetailsResponse>, userId: String, typeId: String, empType: String
    ): ApiResponseListener<UserDetailsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {

                Log.d(TAG, "handleUserDetailResult: $it")
                val userData = UserData(
                    userId,
                    typeId,
                    empType,
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

                Log.d(TAG, "handleUserDetailResult: $userData")

                viewModelScope.launch {
                    userDetailsDao.insertUser(userData)
                }

                return ApiResponseListener.Success(it)
            }
        }
        return ApiResponseListener.Failure(response.message())
    }

    fun getUserDetailsFromRoom() = userDetailsDao.gerUserData()

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteAllUserDataFromRoom() = GlobalScope.launch(Dispatchers.IO) {
        userDetailsDao.deleteAllUserData()
    }



    fun getUserDetailsUpdate(
        appId: String,
        content_type: String,
        userId: String,
        typeId: String,
        empType: String,
        userFullName: String,
        userPartnerNameValue: String
    ) = viewModelScope.launch {

        userDetailsLiveData.postValue(ApiResponseListener.Loading())

        try {
            val response = userDetailsRepository.getUserDetails(appId, content_type, userId, typeId)
            userDetailsLiveData.postValue(handleUserDetailResultUpdate(response, userId, typeId, empType,userFullName,userPartnerNameValue))

        } catch (t: Throwable) {
            when (t) {
                is IOException -> userDetailsLiveData.postValue(
                    ApiResponseListener.Failure(
                        "Network Failure"
                    )
                )
                else -> userDetailsLiveData.postValue(ApiResponseListener.Failure("Conversion Error"))
            }
        }


    }



    private suspend fun handleUserDetailResultUpdate(
        response: Response<UserDetailsResponse>,
        userId: String,
        typeId: String,
        empType: String,
        userFullName: String,
        userPartnerNameValue: String
    ): ApiResponseListener<UserDetailsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {

                if (!userFullName.trim().lowercase(getDefault()).equals(it.name?.trim()
                        ?.lowercase(getDefault()))) {

                    Log.d(TAG, "handleUserDetailResult: $it")
                    val userData = UserData(
                        userId,
                        typeId,
                        empType,
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

                    Log.d(TAG, "handleUserDetailResult: $userData")

                   var job = viewModelScope.launch {
                        userDetailsDao.insertUser(userData)
                    }

                    job.join()


                    return ApiResponseListener.Success(it)

                }else if (!userPartnerNameValue.trim().lowercase(getDefault()).equals(it.partnerName?.trim()
                        ?.lowercase(getDefault()))){

                    if (!userPartnerNameValue.equals("null")) {
                        val userData = UserData(
                            userId,
                            typeId,
                            empType,
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

                        Log.d(TAG, "handleUserDetailResult: $userData")

                       var job = viewModelScope.launch {

                            userDetailsDao.insertUser(userData)
                        }

                        job.join()


                       return ApiResponseListener.Success(it)

                    }

                }
            }
        }

        return ApiResponseListener.Failure(response.message())
    }

}