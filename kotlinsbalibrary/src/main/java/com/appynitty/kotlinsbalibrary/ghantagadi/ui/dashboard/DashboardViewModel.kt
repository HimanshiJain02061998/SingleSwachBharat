package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.model.response.AttendanceResponse
import com.appynitty.kotlinsbalibrary.common.model.response.VehicleQrDetailsResponse
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils.Companion.STATUS_SUCCESS
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.OutPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DumpYardIds
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleNumberResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleTypeResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.DutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val TAG = "DashboardViewModel"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dutyRepository: DutyRepository,
    private val userDataStore: UserDataStore,
    private val sessionDataStore: SessionDataStore,
    private val archivedDao: ArchivedDao,
    private val languageDataStore: LanguageDataStore,
    private val nearestLatLngDao: NearestLatLngDao

) : ViewModel() {

    private val dashboardEventChannel = Channel<DashboardEvent>()
    val dashboardEventsFlow = dashboardEventChannel.receiveAsFlow()

    /**
     *  METHOD TO GET VEHICLE TYPES FROM API
     */
    fun getVehicleTypeDetails() = viewModelScope.launch {

        dashboardEventChannel.send(DashboardEvent.ShowProgressBar)
        dashboardEventChannel.send(DashboardEvent.MakeDutyToggleClickedFalse)

        try {

            val empType1 = userDataStore.getUserEssentials.first().employeeType
            when (empType1) {
                "D" -> {
                    val response = dutyRepository.getDumpYardIds(
                        CommonUtils.APP_ID
                    )
                    handleDumpYardIdsResponse(response)
                }
                else -> {
                    val response = dutyRepository.getVehicleTypeDetails(
                        CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE
                    )
                    handleVehicleTypeResponse(response)
                }
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> DashboardEvent.ShowFailureMessage(
                    "Connection Timeout"
                )

                else -> DashboardEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }
            dashboardEventChannel.send(DashboardEvent.HideProgressBar)
        }
    }

    private fun handleDumpYardIdsResponse(response: Response<List<DumpYardIds>>) =
        viewModelScope.launch {
            if (response.isSuccessful) {
                response.body()?.let {
                    dashboardEventChannel.send(DashboardEvent.ShowDumpYardIdsDialog(it))
                }
            } else {
                dashboardEventChannel.send(DashboardEvent.ShowFailureMessage(response.message()))
            }
            dashboardEventChannel.send(DashboardEvent.HideProgressBar)
        }

    /**
     *  METHOD TO HANDLE RESPONSE OF GET VEHICLE TYPES
     */
    private fun handleVehicleTypeResponse(
        response: Response<List<VehicleTypeResponse>>
    ) = viewModelScope.launch {

        if (response.isSuccessful) {
            response.body()?.let {
                dashboardEventChannel.send(DashboardEvent.ShowVehicleTypeDialog(it))
            }
        } else {
            dashboardEventChannel.send(DashboardEvent.ShowFailureMessage(response.message()))
        }
        dashboardEventChannel.send(DashboardEvent.HideProgressBar)

    }

    /**
     *  METHOD TO GET VEHICLE NUMBERS FROM API
     */
    fun getVehicleNumberList(
        appId: String, content_type: String, vehicleTypeId: String
    ) = viewModelScope.launch {

        try {
            dashboardEventChannel.send(DashboardEvent.ShowDialogProgressBar)
            val response = dutyRepository.getVehicleNumberList(appId, content_type, vehicleTypeId)
            handleVehicleNumberListResponse(response)

        } catch (t: Throwable) {
            dashboardEventChannel.send(DashboardEvent.HideDialogProgressBar)
            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)

            when (t) {
                is IOException -> DashboardEvent.ShowFailureMessage(
                    "Connection Timeout"
                )

                else -> DashboardEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }
        }
    }

    /**
     *  METHOD TO HANDLE RESPONSE OF GET VEHICLE NUMBERS
     */
    private fun handleVehicleNumberListResponse(
        response: Response<List<VehicleNumberResponse>>
    ) = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.HideDialogProgressBar)

        if (response.isSuccessful) {
            response.body()?.let {
                dashboardEventChannel.send(DashboardEvent.ShowVehicleNumberList(it))
            }
        } else {
            dashboardEventChannel.send(DashboardEvent.ShowFailureMessage(response.message()))
        }

    }

    /**
     *  METHOD TO SAVE ATTENDANCE ON TO API
     */
    fun saveInPunchDetails(
        appId: String,
        content_type: String,
        batteryStatus: Int,
        inPunchRequest: InPunchRequest,
        userVehicleDetails: UserVehicleDetails?
    ) = viewModelScope.launch {

        dashboardEventChannel.send(DashboardEvent.ShowProgressBar)

        try {

            val response = dutyRepository.saveInPunchDetails(
                appId, content_type, batteryStatus, inPunchRequest
            )
            handleAttendanceOnResponse(response, userVehicleDetails)

        } catch (t: Throwable) {

            dashboardEventChannel.send(DashboardEvent.HideProgressBar)
            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)

            when (t) {
                is IOException -> DashboardEvent.ShowFailureMessage(
                    "Connection Timeout"
                )

                else -> DashboardEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }

        }
    }

    /**
     * METHOD TO GET VEHICLE QR DETAILS
     */

    fun getVehicleQrDetails(
        appId: String,
        contentType: String,
        referenceId: String,
        empType: String,
        currentLat: String,
        currentLon: String,
        batteryStatus: Int,
        inPunchRequest: InPunchRequest
    ) = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.ShowProgressBar)
        try {

            if (empType == "D") {

                val userVehicleDetails = UserVehicleDetails(
                    "1",
                    "",
                    referenceId
                )

                inPunchRequest.vehicleType = "1"
                inPunchRequest.vehicleNumber = "1"

                saveInPunchDetails(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    batteryStatus,
                    inPunchRequest,
                    userVehicleDetails
                )

            } else {
                val response = dutyRepository.getVehicleQrDetails(
                    appId, contentType, referenceId, empType, currentLat, currentLon
                )

                handleVehicleQrDetailsResponse(response, batteryStatus, inPunchRequest)
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> DashboardEvent.ShowFailureMessage(
                    "Connection Timeout"
                )

                else -> DashboardEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }
            dashboardEventChannel.send(DashboardEvent.HideProgressBar)
            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
        }
    }

    private fun handleVehicleQrDetailsResponse(
        response: Response<VehicleQrDetailsResponse>,
        batteryStatus: Int,
        inPunchRequest: InPunchRequest
    ) =
        viewModelScope.launch {

            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
            dashboardEventChannel.send(DashboardEvent.HideProgressBar)

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.status == STATUS_SUCCESS) {


                        if (response.body() != null) {
                            val userVehicleDetails = UserVehicleDetails(
                                response.body()!!.vtId,
                                response.body()!!.vehicleType,
                                response.body()!!.vehicleNumber
                            )

                            inPunchRequest.vehicleType = response.body()!!.vtId
                            inPunchRequest.vehicleNumber = response.body()!!.vehicleNumber

                            saveInPunchDetails(
                                CommonUtils.APP_ID,
                                CommonUtils.CONTENT_TYPE,
                                batteryStatus,
                                inPunchRequest,
                                userVehicleDetails
                            )

                        }


                    } else {
                        dashboardEventChannel.send(
                            DashboardEvent.ShowResponseErrorMessage(
                                it.message, it.messageMar
                            )
                        )
                    }
                }
            } else {
                dashboardEventChannel.send(
                    DashboardEvent.ShowFailureMessage(
                        response.code().toString()
                    )
                )
            }
        }

    /**
     *  METHOD TO HANDLE RESPONSE OF SAVE ATTENDANCE ON TO API
     */
    private fun handleAttendanceOnResponse(
        response: Response<AttendanceResponse>,
        userVehicleDetails: UserVehicleDetails?
    ) = viewModelScope.launch {

        dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
        dashboardEventChannel.send(DashboardEvent.HideProgressBar)

        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == STATUS_SUCCESS) {
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseSuccessMessage(
                            it.message, it.messageMar
                        )
                    )
                    dashboardEventChannel.send(DashboardEvent.StartLocationTracking)
                    if (userVehicleDetails == null)
                        dashboardEventChannel.send(DashboardEvent.SaveVehicleDetails)
                    else
                        saveUserVehicleDetails(userVehicleDetails)


                    saveUserIsDutyOn(true)
                    saveUserDutyOnDate(DateTimeUtils.getServerDate())
                    saveGisTrailId(UUID.randomUUID().toString())
                    saveGisStartTs(DateTimeUtils.getGisServiceTimeStamp())

                    // user_Detailes_Update

                    dashboardEventChannel.send(DashboardEvent.UserDetailsUpdate)


                } else {
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseErrorMessage(
                            it.message, it.messageMar
                        )
                    )
                }
            }
        } else {
            dashboardEventChannel.send(
                DashboardEvent.ShowFailureMessage(
                    response.code().toString()
                )
            )
        }

    }

    /**
     *  METHOD TO SAVE ATTENDANCE OFF TO API
     */
    fun saveOutPunchDetails(
        appId: String, content_type: String, batteryStatus: Int, outPunchRequest: OutPunchRequest
    ) = viewModelScope.launch {

        dashboardEventChannel.send(DashboardEvent.ShowProgressBar)
        val trailId = sessionDataStore.getGisTrailId.first()

        try {

            val userDetails = userDataStore.getUserEssentials.first()
            if (userDetails.employeeType == "D") {
                val vehicleDetails = userDataStore.getUserVehicleDetails.first()
                outPunchRequest.ReferanceId = vehicleDetails.vehicleNumber
            }
            val response = dutyRepository.saveOutPunchDetails(
                appId, content_type, batteryStatus, trailId, outPunchRequest
            )
            handleAttendanceOffResponse(response)

        } catch (t: Throwable) {
            when (t) {
                is IOException -> DashboardEvent.ShowFailureMessage(
                    "Connection Timeout"
                )

                else -> DashboardEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }
            dashboardEventChannel.send(DashboardEvent.HideProgressBar)
            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
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
                if (it.status == STATUS_SUCCESS) {
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseSuccessMessage(
                            it.message, it.messageMar
                        )
                    )
                    dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
                    //  dashboardEventChannel.send(DashboardEvent.HitGisServer)
                    saveUserIsDutyOn(false)

                } else {
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseErrorMessage(
                            it.message, it.messageMar
                        )
                    )
                }
            }
        } else {
            dashboardEventChannel.send(
                DashboardEvent.ShowFailureMessage(
                    response.code().toString()
                )
            )
        }
        dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
        dashboardEventChannel.send(DashboardEvent.HideProgressBar)
    }

    val userVehicleDetailsFlow = userDataStore.getUserVehicleDetails.asLiveData()
    val isBifurcationOnLiveData = userDataStore.getIsBifurcationOn.asLiveData()
    val isVehicleScanOnLiveData = userDataStore.getIsVehicleScanOn.asLiveData()
    val userLocationLiveData = userDataStore.getUserLatLong.asLiveData()
    val userEssentialsFlow = userDataStore.getUserEssentials

    fun saveUserVehicleDetails(vehicleDetails: UserVehicleDetails) = viewModelScope.launch {
        userDataStore.saveUserVehicleDetails(vehicleDetails)
    }

    fun saveUserLocation(userLatLong: UserLatLong) = viewModelScope.launch {
        userDataStore.saveUserLatLong(userLatLong)
    }

    fun saveIsBifurcationOn(isBifurcationOn: Boolean) = viewModelScope.launch {
        userDataStore.saveIsBifurcationOn(isBifurcationOn)
    }

    fun saveIsVehicleScanOn(isVehicleScanOn: Boolean) = viewModelScope.launch {
        userDataStore.saveIsVehicleScanOn(isVehicleScanOn)
    }

    val isUserLoggedInLiveData = sessionDataStore.getIsUserLoggedIn.asLiveData()
    val isUserDutyOnFlow = sessionDataStore.getIsUserDutyOn
    private val dutyOnDateFlow = sessionDataStore.getDutyOnDate

    private fun saveUserIsDutyOn(isDutyOn: Boolean) = viewModelScope.launch {
        sessionDataStore.saveIsUserDutyOn(isDutyOn)
    }

    private fun saveDumpYardTripNoToZero() = viewModelScope.launch {
        sessionDataStore.saveDumpYardTripNo(0)
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

    private fun deleteAllArchivedData() =
        viewModelScope.launch { archivedDao.deleteAllArchivedData() }

    fun checkIsDateChanged() = viewModelScope.launch {

        val dutyDate = dutyOnDateFlow.first()
        val currentDate = DateTimeUtils.getServerDate()

        if (dutyDate.isNotEmpty()) {
            if (dutyDate != currentDate) {

                sessionDataStore.saveIsUserDutyOn(false)
                deleteAllArchivedData()
                saveDumpYardTripNoToZero()
                nearestLatLngDao.deleteAllNearestHouses()
                userDataStore.saveLastGhantaGadiScanLatLong(UserLatLong("", "", "0"))
            }
            dashboardEventChannel.send(DashboardEvent.CheckIfServiceIsRunning)
        }
    }

    fun shouldStartLocationService(isServiceRunning: Boolean) = viewModelScope.launch {

        val isDutyOn = sessionDataStore.getIsUserDutyOn.first()

        if (isDutyOn) {
            if (!isServiceRunning) {

                dashboardEventChannel.send(DashboardEvent.StartLocationTracking)
            }
        } else {
            if (isServiceRunning) dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
        }
    }

    fun onQrMenuClicked(isDutyOn: Boolean) = viewModelScope.launch {
        if (isDutyOn) {
            val isGtFeatureOn = userDataStore.getIsBifurcationOn.first()
            dashboardEventChannel.send(DashboardEvent.NavigateToQrScreen(isGtFeatureOn))
        } else {
            dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.be_no_duty))
        }
    }

    fun onTakePhotoMenuClicked(isDutyOn: Boolean) = viewModelScope.launch {
        if (isDutyOn) {
            dashboardEventChannel.send(DashboardEvent.NavigateToTakePhotoScreen)
        } else {
            dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.be_no_duty))
        }
    }

    fun onMyLocationMenuClicked(isDutyOn: Boolean) = viewModelScope.launch {
      //  if (isDutyOn) {
            dashboardEventChannel.send(DashboardEvent.NavigateToMyLocationScreen)
     //   } else {
       //     dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.be_no_duty))
      //  }
    }

    fun onWorkHistoryMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.NavigateToWorkHistoryScreen)
    }

    fun onSyncMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.NavigateToSyncOfflineScreen)
    }

    fun onProfileMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.NavigateToProfileScreen)
    }

    fun onPrivacyPolicyFabMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.NavigateToPrivacyPolicyScreen)
    }

    fun onSettingFabMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.ShowSettingScreen)
    }

    fun onChangeLanguageFabMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.ShowChangeLanguageScreen)
    }

    fun onLogOutFabMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(
            DashboardEvent.ShowAlertDialog(
                R.string.logout_confirmation_title,
                R.string.logout_confirmation_msg,
                CommonUtils.CONFIRM_LOGOUT_DIALOG
            )
        )
    }

    fun onDutyToggleClicked(
        isInternetOn: Boolean,
        isGpsOn: Boolean,
        isChecked: Boolean,
        isVehicleScanOn: Boolean,
        userId: String?,
        batteryStatus: Int
    ) = viewModelScope.launch {

    if (isInternetOn && isGpsOn) {
            val isDutyOn = isUserDutyOnFlow.first()
            if (isChecked) {
                if (!isDutyOn) {

                    when (val empType = userDataStore.getUserEssentials.first().employeeType) {
                        "N" -> {
//                            if (!isVehicleScanOn) dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
//                            else
                            dashboardEventChannel.send(DashboardEvent.StartVehicleQrScanner)
                            // dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
                        }

                        "D" -> {
                            if (!isVehicleScanOn) dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
                            else dashboardEventChannel.send(DashboardEvent.StartVehicleQrScanner)
                        }

                        else -> {
                            val userVehicleDetails = UserVehicleDetails(
                                "1",
                                "",
                                "1"
                            )

                            val latitude = userLocationLiveData.value?.latitude
                            val longitude = userLocationLiveData.value?.longitude

                            val inPunchRequest = userId?.let {
                                InPunchRequest(
                                    DateTimeUtils.getServerTime(),
                                    DateTimeUtils.getYyyyMMddDate(),
                                    latitude!!,
                                    longitude!!,
                                    it,
                                    "1",
                                    "1",
                                    empType,
                                    ""
                                )
                            }

                            saveInPunchDetails(
                                CommonUtils.APP_ID,
                                CommonUtils.CONTENT_TYPE,
                                batteryStatus,
                                inPunchRequest!!,
                                userVehicleDetails
                            )
                        }
                    }
                }
            } else {
                if (isDutyOn) {
                    dashboardEventChannel.send(
                        DashboardEvent.ShowAlertDialog(
                            R.string.offduty_confirmation_title,
                            R.string.offduty_confirmation_msg,
                            CommonUtils.CONFIRM_OFF_DUTY_DIALOG
                        )
                    )
                }
            }
        } else {
//            if (!isInternetOn && isGpsOn) {
//                dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.no_internet_error))
//                dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
//            }else if(!isInternetOn){
//
//            }
        if (!isGpsOn) {
            dashboardEventChannel.send(DashboardEvent.TurnGpsOn)
        } else {
            dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.no_internet_error))
            dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
        }


    }
    }

    fun onAlertDialogYesBtnClicked(type: String, isDutyOn: Boolean, gcCount: Int) =
        viewModelScope.launch {
            if (type == CommonUtils.CONFIRM_LOGOUT_DIALOG) {
                if (gcCount > 0) {
                    dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.sync_offline_data_warning))
                    dashboardEventChannel.send(DashboardEvent.DismissAlertDialogFrag)
                } else if (isDutyOn) {
                    dashboardEventChannel.send(DashboardEvent.ShowWarningMessage(R.string.off_duty_warning))
                    dashboardEventChannel.send(DashboardEvent.DismissAlertDialogFrag)
                } else {

                    userDataStore.clearUserDatastore()
                    sessionDataStore.clearSessionDatastore()
                    archivedDao.deleteAllArchivedData()
                    dashboardEventChannel.send(DashboardEvent.NavigateToLoginScreen)

                }
            } else if (type == CommonUtils.CONFIRM_OFF_DUTY_DIALOG) {

                dashboardEventChannel.send(DashboardEvent.TurnDutyOff)
                dashboardEventChannel.send(DashboardEvent.DismissAlertDialogFrag)
            }
        }

    fun onLanguageDialogSubmitBtnClicked(appLanguage: AppLanguage) = viewModelScope.launch {
        languageDataStore.savePreferredLanguage(appLanguage)
        dashboardEventChannel.send(DashboardEvent.RestartDashboardActivity)
        dashboardEventChannel.send(DashboardEvent.DismissLanguageDialog)
    }


    sealed class DashboardEvent {

        data class NavigateToQrScreen(val isGtFeatureOn: Boolean) : DashboardEvent()
        object NavigateToTakePhotoScreen : DashboardEvent()
        object NavigateToMyLocationScreen : DashboardEvent()
        object NavigateToWorkHistoryScreen : DashboardEvent()
        object NavigateToSyncOfflineScreen : DashboardEvent()
        object NavigateToProfileScreen : DashboardEvent()
        object NavigateToPrivacyPolicyScreen : DashboardEvent()
        object NavigateToLoginScreen : DashboardEvent()
        object ShowSettingScreen : DashboardEvent()
        object ShowChangeLanguageScreen : DashboardEvent()
        object DismissAlertDialogFrag : DashboardEvent()
        object EnableDutyToggle : DashboardEvent()
        object TurnGpsOn : DashboardEvent()
        object TurnDutyOff : DashboardEvent()
        object DismissLanguageDialog : DashboardEvent()
        object RestartDashboardActivity : DashboardEvent()
        object GetVehiclesData : DashboardEvent()
        object StartVehicleQrScanner : DashboardEvent()
        object ShowProgressBar : DashboardEvent()
        object HideProgressBar : DashboardEvent()
        object StartLocationTracking : DashboardEvent()
        object StopLocationTracking : DashboardEvent()
        object HitGisServer : DashboardEvent()
        object SaveVehicleDetails : DashboardEvent()

        object MakeDutyToggleClickedFalse : DashboardEvent()
        object CheckIfServiceIsRunning : DashboardEvent()

        object ShowDialogProgressBar : DashboardEvent()
        object HideDialogProgressBar : DashboardEvent()

        data class ShowVehicleTypeDialog(val vehicleTypeList: List<VehicleTypeResponse>) :
            DashboardEvent()
        data class ShowDumpYardIdsDialog(val dumpYardIdsList: List<DumpYardIds>) :
            DashboardEvent()
        data class ShowVehicleNumberList(val vehicleNumberList: List<VehicleNumberResponse>) :
            DashboardEvent()

        data class ShowWarningMessage(val resourceId: Int) : DashboardEvent()
        data class ShowFailureMessage(val msg: String) : DashboardEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            DashboardEvent()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : DashboardEvent()
        data class ShowAlertDialog(
            val titleResourceId: Int, val messageResourceId: Int, val dialogType: String
        ) : DashboardEvent()


        object UserDetailsUpdate : DashboardEvent()

    }
}

