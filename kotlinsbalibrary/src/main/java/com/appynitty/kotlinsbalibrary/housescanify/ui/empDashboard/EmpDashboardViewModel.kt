package com.appynitty.kotlinsbalibrary.housescanify.ui.empDashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.api.GisApi
import com.appynitty.kotlinsbalibrary.common.dao.GisLocDao
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.model.response.AttendanceResponse
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.TempUserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserEssentials
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.PropertyTypeDao
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchInRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchOutRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpDutyRepository
import com.appynitty.kotlinsbalibrary.housescanify.repository.PropertyTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val TAG = "EmpDashboardViewModel"

@HiltViewModel
class EmpDashboardViewModel @Inject constructor(
    private val empDutyRepository: EmpDutyRepository,
    private val sessionDataStore: SessionDataStore,
    private val userDataStore: UserDataStore,
    private val languageDataStore: LanguageDataStore,
    private val archivedDao: ArchivedDao,
    private val houseOnMapDao: EmpHouseOnMapDao,
    private val gisLocDao: GisLocDao,
    private val gisApi: GisApi,
    private val propertyTypeDao: PropertyTypeDao,
    private val propertyTypeRepository: PropertyTypeRepository,
    private val userTravelLocDao: UserTravelLocDao,
    private val nearestLatLngDao: NearestLatLngDao,
    private val garbageCollectionDao: GarbageCollectionDao,
    private val tempUserDataStore: TempUserDataStore,
) : ViewModel() {


    private val empDashboardEventChannel = Channel<EmpDashboardEvent>()
    val empDashboardEventsFlow = empDashboardEventChannel.receiveAsFlow()

    /**
     *  METHOD TO SAVE ATTENDANCE ON TO API
     */

    suspend fun checkSameUserLogin(): Boolean {
        val tempUser = tempUserDataStore.getUserEssentials.first()
        val user = userDataStore.getUserEssentials.first()

        val tempId = tempUser.userId
        val userId = user.userId

        // Return true if IDs are same OR either is empty/null
        return tempId.isNullOrEmpty() || tempId == userId
    }

    fun saveEmpPunchInDetails(
        appId: String,
        content_type: String,
        inPunchRequest: EmpPunchInRequest
    ) = viewModelScope.launch {

        empDashboardEventChannel.send(EmpDashboardEvent.ShowProgressBar)

        try {
            val response =
                empDutyRepository.saveEmpPunchInDetails(
                    appId,
                    content_type,
                    inPunchRequest
                )
            handleAttendanceOnResponse(response)

        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    EmpDashboardEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else ->
                    EmpDashboardEvent.ShowFailureMessage(
                        "Conversion Error"
                    )
            }
            empDashboardEventChannel.send(EmpDashboardEvent.EnableDutyToggle)
            empDashboardEventChannel.send(EmpDashboardEvent.HideProgressBar)
        }
    }

    /**
     *  METHOD TO HANDLE RESPONSE OF SAVE ATTENDANCE ON TO API
     */
    private fun handleAttendanceOnResponse(
        response: Response<AttendanceResponse>
    ) = viewModelScope.launch {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == CommonUtils.STATUS_SUCCESS) {
                    empDashboardEventChannel.send(
                        EmpDashboardEvent.ShowResponseSuccessMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                    empDashboardEventChannel.send(EmpDashboardEvent.StartLocationTracking)

                    saveUserIsDutyOn(true)
                    saveUserDutyOnDate(DateTimeUtils.getServerDate())
                    saveGisTrailId(UUID.randomUUID().toString())
                    saveGisStartTs(DateTimeUtils.getGisServiceTimeStamp())


                    // UserDetailsUpdate

                    empDashboardEventChannel.send(EmpDashboardEvent.UserDetailsUpdate)

                } else {
                    empDashboardEventChannel.send(
                        EmpDashboardEvent.ShowResponseErrorMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                }
            }
        } else {
            empDashboardEventChannel.send(EmpDashboardEvent.ShowFailureMessage(response.message()))
        }
        empDashboardEventChannel.send(EmpDashboardEvent.EnableDutyToggle)
        empDashboardEventChannel.send(EmpDashboardEvent.HideProgressBar)
    }

    /**
     *  METHOD TO SAVE ATTENDANCE OFF TO API
     */
    fun saveOutPunchDetails(
        appId: String,
        content_type: String,
        outPunchRequest: EmpPunchOutRequest
    ) = viewModelScope.launch {

        // empDashboardEventChannel.send(EmpDashboardEvent.HitGisServer)

        empDashboardEventChannel.send(EmpDashboardEvent.ShowProgressBar)

        val trailId = sessionDataStore.getGisTrailId.first()
        try {
            val response =
                empDutyRepository.saveEmpPunchOutDetails(
                    appId,
                    content_type,
                    trailId,
                    outPunchRequest
                )
            handleAttendanceOffResponse(response)

        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    EmpDashboardEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else ->
                    EmpDashboardEvent.ShowFailureMessage(
                        "Conversion Error"
                    )
            }
            empDashboardEventChannel.send(EmpDashboardEvent.HideProgressBar)
        }
    }

    /**
     *  METHOD TO HANDLE RESPONSE OF SAVE ATTENDANCE OFF TO API
     */
    private fun handleAttendanceOffResponse(
        response: Response<AttendanceResponse>
    ) = viewModelScope.launch {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == CommonUtils.STATUS_SUCCESS) {
                    empDashboardEventChannel.send(
                        EmpDashboardEvent.ShowResponseSuccessMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                    empDashboardEventChannel.send(EmpDashboardEvent.StopLocationTracking)
                    saveUserIsDutyOn(false)

                } else {
                    empDashboardEventChannel.send(
                        EmpDashboardEvent.ShowResponseErrorMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                }
            }
        } else {
            empDashboardEventChannel.send(EmpDashboardEvent.ShowFailureMessage(response.message()))
        }
        empDashboardEventChannel.send(EmpDashboardEvent.EnableDutyToggle)
        empDashboardEventChannel.send(EmpDashboardEvent.HideProgressBar)
    }

    val userLocationLiveData = userDataStore.getUserLatLong.asLiveData()
    val userEssentialsFlow = userDataStore.getUserEssentials

    fun saveUserLocation(userLatLong: UserLatLong) = viewModelScope.launch {
        userDataStore.saveUserLatLong(userLatLong)
    }

    val isUserLoggedInLiveData = sessionDataStore.getIsUserLoggedIn.asLiveData()
    val isUserDutyOnFlow = sessionDataStore.getIsUserDutyOn
    private val dutyOnDateFlow = sessionDataStore.getDutyOnDate

    private fun saveUserIsDutyOn(isDutyOn: Boolean) = viewModelScope.launch {
        sessionDataStore.saveIsUserDutyOn(isDutyOn)
    }

    private fun saveUserDutyOnDate(dutyOnDate: String) = viewModelScope.launch {
        sessionDataStore.saveUserDutyOnDate(dutyOnDate)
    }

    private fun saveGisTrailId(trailId: String) = viewModelScope.launch {
        sessionDataStore.saveGisTrailId(trailId)
    }

    private fun saveGisStartTs(gisStartTs: String) = viewModelScope.launch {
        sessionDataStore.saveGisStartTs(gisStartTs)
    }

    fun checkIsDateChanged() = viewModelScope.launch {
        val dutyDate = dutyOnDateFlow.first()
        val currentDate = DateTimeUtils.getServerDate()

        if (dutyDate.isNotEmpty()) {

            if (dutyDate != currentDate) {
                sessionDataStore.saveIsUserDutyOn(false)
                archivedDao.deleteAllArchivedData()
                sessionDataStore.saveBearerToken("")
                houseOnMapDao.deleteAllHouseOnMapData()
                userDataStore.saveLastHouseScanifyLatLong(
                    UserLatLong(
                        "",
                        "",
                        "0"
                    )
                )
            }
            empDashboardEventChannel.send(EmpDashboardEvent.CheckIfServiceIsRunning)
        } else {
            houseOnMapDao.deleteAllHouseOnMapData()
        }

//        val tempList = propertyTypeDao.getAllPropertyTypes().first()
//        if (tempList.isEmpty()){
//            getAllPropertyTypesFromApi(CommonUtils.APP_ID)
//        }


    }

    fun shouldStartLocationService(isServiceRunning: Boolean) = viewModelScope.launch {

        val isDutyOn = sessionDataStore.getIsUserDutyOn.first()

        if (isDutyOn) {
            if (!isServiceRunning) {
                empDashboardEventChannel.send(EmpDashboardEvent.StartLocationTracking)
            }
        } else {
            if (isServiceRunning)
                empDashboardEventChannel.send(EmpDashboardEvent.StopLocationTracking)
        }
    }


    fun onDutyToggleClicked(isInternetOn: Boolean, isGpsOn: Boolean, isChecked: Boolean) =
        viewModelScope.launch {

            if (isInternetOn && isGpsOn) {
                val isDutyOn = isUserDutyOnFlow.first()
                if (isChecked) {
                    if (!isDutyOn) {
                        empDashboardEventChannel.send(EmpDashboardEvent.TurnDutyOn)
                    }
                } else {
                    if (isDutyOn) {
                        empDashboardEventChannel.send(
                            EmpDashboardEvent.ShowAlertDialog(
                                R.string.offduty_confirmation_title,
                                R.string.offduty_confirmation_msg,
                                CommonUtils.CONFIRM_OFF_DUTY_DIALOG
                            )
                        )
                    }
                }
            } else {
                if (!isInternetOn) {
                    empDashboardEventChannel.send(
                        EmpDashboardEvent.ShowWarningMessage(
                            R.string.no_internet_error
                        )
                    )
                    empDashboardEventChannel.send(EmpDashboardEvent.EnableDutyToggle)
                }
                if (!isGpsOn) {
                    empDashboardEventChannel.send(EmpDashboardEvent.TurnGpsOn)
                }
            }
        }

    fun onAlertDialogYesBtnClicked(type: String, isDutyOn: Boolean, gcCount: Int) =
        viewModelScope.launch {
            if (type == CommonUtils.CONFIRM_LOGOUT_DIALOG) {
                if (gcCount > 0) {
                    empDashboardEventChannel.send(EmpDashboardEvent.ShowWarningMessage(R.string.sync_offline_data_warning))
                    empDashboardEventChannel.send(EmpDashboardEvent.DismissAlertDialogFrag)
                } else if (isDutyOn) {
                    empDashboardEventChannel.send(EmpDashboardEvent.ShowWarningMessage(R.string.off_duty_warning))
                    empDashboardEventChannel.send(EmpDashboardEvent.DismissAlertDialogFrag)
                } else {
                    userDataStore.clearUserDatastore()
                    sessionDataStore.clearSessionDatastore()
                    archivedDao.deleteAllArchivedData()
                    houseOnMapDao.deleteAllHouseOnMapData()
                    propertyTypeDao.deleteAllProperties()
                    empDashboardEventChannel.send(EmpDashboardEvent.NavigateToLoginScreen)
                }
            } else if (type == CommonUtils.CONFIRM_OFF_DUTY_DIALOG) {
                empDashboardEventChannel.send(EmpDashboardEvent.TurnDutyOff)
                empDashboardEventChannel.send(EmpDashboardEvent.DismissAlertDialogFrag)
            }
        }

    fun onLogOutFabMenuClicked() = viewModelScope.launch {
        empDashboardEventChannel.send(
            EmpDashboardEvent.ShowAlertDialog(
                R.string.logout_confirmation_title,
                R.string.logout_confirmation_msg,
                CommonUtils.CONFIRM_LOGOUT_DIALOG
            )
        )
    }

    fun onChangeLanguageFabMenuClicked() = viewModelScope.launch {
        empDashboardEventChannel.send(EmpDashboardEvent.ShowChangeLanguageScreen)
    }

    fun onPrivacyPolicyFabMenuClicked() = viewModelScope.launch {
        empDashboardEventChannel.send(EmpDashboardEvent.NavigateToPrivacyPolicyScreen)
    }

    fun onQrMenuClicked(isDutyOn: Boolean) = viewModelScope.launch {
        if (isDutyOn) {
            empDashboardEventChannel.send(EmpDashboardEvent.NavigateToEmpQrScreen)
        } else {
            empDashboardEventChannel.send(EmpDashboardEvent.ShowWarningMessage(R.string.be_no_duty))
        }
    }

    fun onWorkHistoryMenuClicked() = viewModelScope.launch {
        empDashboardEventChannel.send(EmpDashboardEvent.NavigateToEmpWorkHistoryScreen)
    }
    fun onMyLocationMenuClicked(isDutyOn: Boolean) = viewModelScope.launch {
        //  if (isDutyOn) {
        empDashboardEventChannel.send(EmpDashboardEvent.NavigateToMyLocationScreen)
        //   } else {
        //     dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.be_no_duty))
        //  }
    }
    fun onSyncOfflineMenuClicked() = viewModelScope.launch {
        empDashboardEventChannel.send(EmpDashboardEvent.NavigateToEmpSyncOfflineScreen)
    }

    fun onLanguageDialogSubmitBtnClicked(appLanguage: AppLanguage) = viewModelScope.launch {
        languageDataStore.savePreferredLanguage(appLanguage)
        empDashboardEventChannel.send(EmpDashboardEvent.RestartDashboardActivity)
        empDashboardEventChannel.send(EmpDashboardEvent.DismissLanguageDialog)
    }


    fun getAllPropertyTypesFromApi(appId: String) = viewModelScope.launch {

        try {
            val response = propertyTypeRepository.getAllPropertyTypes(appId)
            handlePropertyTypeResponse(response)
        }catch (t: Throwable) {

            when (t) {
                is IOException -> Log.e(TAG, "sendGisLocation: ${t.message}")
                else -> Log.e(TAG, "sendGisLocation: ${t.message}")
            }

        }

    }

    private fun handlePropertyTypeResponse(response: Response<List<PropertyType>>) =
        viewModelScope.launch {

            if (!response.body().isNullOrEmpty()){
                propertyTypeDao.deleteAllProperties()

                response.body()?.forEach {
                    propertyTypeDao.insertPropertyDao(it)
                }

            }

        }

    fun performForcefullyLogout(){
        viewModelScope.launch {
            val userDetails = userDataStore.getUserEssentials.first()
            tempUserDataStore.saveUserEssentials(UserEssentials(userDetails.userId,userDetails.employeeType,userDetails.userTypeId))
            userDataStore.clearUserDatastore()
            sessionDataStore.clearSessionDatastore()
//            archivedDao.deleteAllArchivedData()
//            userTravelLocDao.deleteAllUserTravelLatLongs()
//            nearestLatLngDao.deleteAllNearestHouses()
//            garbageCollectionDao.deleteAllGarbageCollection()
            empDashboardEventChannel.send(EmpDashboardEvent.StopLocationTracking)
            empDashboardEventChannel.send(EmpDashboardEvent.NavigateToLoginScreen)
        }
    }

    fun clearAllDataNewUser(){
        viewModelScope.launch {
            archivedDao.deleteAllArchivedData()
            userTravelLocDao.deleteAllUserTravelLatLongs()
            nearestLatLngDao.deleteAllNearestHouses()
            garbageCollectionDao.deleteAllGarbageCollection()
        }
    }



    sealed class EmpDashboardEvent {

        object NavigateToLoginScreen : EmpDashboardEvent()
        object NavigateToEmpQrScreen : EmpDashboardEvent()
        object NavigateToEmpSyncOfflineScreen : EmpDashboardEvent()
        object NavigateToEmpWorkHistoryScreen : EmpDashboardEvent()
        object NavigateToPrivacyPolicyScreen : EmpDashboardEvent()
        object ShowChangeLanguageScreen : EmpDashboardEvent()
        object NavigateToMyLocationScreen : EmpDashboardEvent()

        data class ShowWarningMessage(val resourceId: Int) : EmpDashboardEvent()
        object EnableDutyToggle : EmpDashboardEvent()
        object TurnGpsOn : EmpDashboardEvent()
        object TurnDutyOff : EmpDashboardEvent()
        object TurnDutyOn : EmpDashboardEvent()
        object DismissAlertDialogFrag : EmpDashboardEvent()
        object DismissLanguageDialog : EmpDashboardEvent()
        object RestartDashboardActivity : EmpDashboardEvent()
        object ShowProgressBar : EmpDashboardEvent()
        object HideProgressBar : EmpDashboardEvent()
        object StartLocationTracking : EmpDashboardEvent()
        object StopLocationTracking : EmpDashboardEvent()
        object CheckIfServiceIsRunning : EmpDashboardEvent()
        object HitGisServer : EmpDashboardEvent()

        data class ShowFailureMessage(val msg: String) : EmpDashboardEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            EmpDashboardEvent()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) :
            EmpDashboardEvent()

        data class ShowAlertDialog(
            val titleResourceId: Int,
            val messageResourceId: Int,
            val dialogType: String
        ) : EmpDashboardEvent()


        object UserDetailsUpdate: EmpDashboardEvent()

    }

}