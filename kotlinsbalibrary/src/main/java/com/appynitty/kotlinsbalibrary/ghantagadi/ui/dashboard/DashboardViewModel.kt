package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.appynitty.kotlinsbalibrary.common.utils.datastore.TempUserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserEssentials
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.OutPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
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
    private val nearestLatLngDao: NearestLatLngDao,
    private val userTravelLocDao: UserTravelLocDao,
    private val garbageCollectionDao: GarbageCollectionDao,
    private val tempUserDataStore: TempUserDataStore
) : ViewModel() {

    private val dashboardEventChannel = Channel<DashboardEvent>(Channel.BUFFERED)
    val dashboardEventsFlow = dashboardEventChannel.receiveAsFlow()

    private val _isTeamSelected = MutableLiveData(false)
    val isTeamSelected: LiveData<Boolean>
        get() =
            _isTeamSelected

    private val _teamMembersSelected = MutableLiveData(emptyList<AvailableEmpItem>())
    val teamMembersSelected: LiveData<List<AvailableEmpItem>>
        get() =
            _teamMembersSelected


    private var deviceIdCon: String? = null

    init {
        getTeam()
        getSelectedTeam()
    }

    /**
     *  METHOD TO GET VEHICLE TYPES FROM API
     */

    suspend fun checkSameUserLogin(): Boolean {
        val tempUser = tempUserDataStore.getUserEssentials.first()
        val user = userDataStore.getUserEssentials.first()

        val tempId = tempUser.userId
        val userId = user.userId

        // Return true if IDs are same OR either is empty/null
        return tempId.isNullOrEmpty() || tempId == userId
    }

    fun getVehicleTypeDetails() = viewModelScope.launch {

        dashboardEventChannel.send(DashboardEvent.ShowProgressBar)
        dashboardEventChannel.trySend(DashboardEvent.MakeDutyToggleClickedFalse).isSuccess

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
        try {
            // Show progress bar event on main thread
            dashboardEventChannel.trySend(DashboardEvent.ShowProgressBar)
            val response = dutyRepository.saveInPunchDetails(
                appId, content_type, batteryStatus, deviceIdCon, inPunchRequest
            )
            handleAttendanceOnResponse(response, userVehicleDetails)
        } catch (t: Throwable) {
            dashboardEventChannel.trySend(DashboardEvent.HideProgressBar)
            dashboardEventChannel.trySend(DashboardEvent.EnableDutyToggle)

            val message = if (t is IOException) "Connection Timeout" else "Conversion Error"
            dashboardEventChannel.trySend(DashboardEvent.ShowFailureMessage(message))
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
    private suspend fun handleAttendanceOnResponse(
        response: Response<AttendanceResponse>,
        userVehicleDetails: UserVehicleDetails?
    ) {
        dashboardEventChannel.trySend(DashboardEvent.HideProgressBar)
        dashboardEventChannel.trySend(DashboardEvent.EnableDutyToggle)

        if (response.isSuccessful) {
            response.body()?.let { body ->
                if (body.status == STATUS_SUCCESS) {
                    dashboardEventChannel.trySend(
                        DashboardEvent.ShowResponseSuccessMessage(body.message, body.messageMar)
                    )
                    dashboardEventChannel.trySend(DashboardEvent.StartLocationTracking)

                    if (userVehicleDetails == null) {
                        dashboardEventChannel.trySend(DashboardEvent.SaveVehicleDetails)
                    } else {
                            saveUserVehicleDetails(userVehicleDetails)
                    }

                    saveUserIsDutyOn(true)
                    saveUserDutyOnDate(DateTimeUtils.getServerDate())
                    saveGisTrailId(UUID.randomUUID().toString())
                    saveGisStartTs(DateTimeUtils.getGisServiceTimeStamp())

                    dashboardEventChannel.trySend(DashboardEvent.UserDetailsUpdate)

                } else {
                    dashboardEventChannel.trySend(
                        DashboardEvent.ShowResponseErrorMessage(body.message, body.messageMar)
                    )
                }
            }
        } else if (response.code() == 422) {
            Log.d("messgeboy", "messages ${response.body()}")
            dashboardEventChannel.trySend(
                DashboardEvent.ShowResponseErrorMessage(
                    "Invalid IMEI No", "अवैध IMEI No"
                )
            )
            performForcefullyLogout()
        } else {
            dashboardEventChannel.trySend(
                DashboardEvent.ShowFailureMessage(response.code().toString())
            )
        }
    }

    fun setTeamSelected(selected: Boolean) {
        viewModelScope.launch {
            userDataStore.saveVewTeam(selected)
        }
    }


    fun getTeam() {
        viewModelScope.launch {
            userDataStore.getVewTeam.collect { value ->
                _isTeamSelected.value = value
            }
        }
    }

    fun getSelectedTeam() {
        viewModelScope.launch {
            userDataStore.getSelectedMembersFlow.collect { value ->
                _teamMembersSelected.value = value
            }
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
                appId, content_type, batteryStatus, trailId, deviceIdCon, outPunchRequest
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

            response.body()?.let { it ->

                if (it.status == STATUS_SUCCESS) {

                    // Success case
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseSuccessMessage(it.message, it.messageMar)
                    )
                    dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
                    saveUserIsDutyOn(false)

                } else {

                    // ERROR case but API returns valid body
                    dashboardEventChannel.send(
                        DashboardEvent.ShowResponseErrorMessage(it.message, it.messageMar)
                    )

                    //  FORCEFUL CHECKOUT CONDITIONS
                    if (it.referenceID == null ||
                        it.isAttendenceOff == null ||
                        it.dutyStatus == null
                    ) {
                        dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
                        saveUserIsDutyOn(false)
                    }
                }
            }

        } else if (response.code() == 422) {

            dashboardEventChannel.send(
                DashboardEvent.ShowResponseErrorMessage("Invalid IMEI No", "अवैध IMEI No")
            )
            performForcefullyLogout()

        } else {
            dashboardEventChannel.send(
                DashboardEvent.ShowFailureMessage(response.code().toString())
            )
        }

        dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
        dashboardEventChannel.send(DashboardEvent.HideProgressBar)
    }

//    private fun handleAttendanceOffResponse(
//        response: Response<AttendanceResponse>
//    ) = viewModelScope.launch {
//
//        if (response.isSuccessful) {
//            response.body()?.let {
//                if (it.status == STATUS_SUCCESS) {
//                    dashboardEventChannel.send(
//                        DashboardEvent.ShowResponseSuccessMessage(
//                            it.message, it.messageMar
//                        )
//                    )
//                    dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
//                    //  dashboardEventChannel.send(DashboardEvent.HitGisServer)
//                    saveUserIsDutyOn(false)
//
//                } else {
//                    dashboardEventChannel.send(
//                        DashboardEvent.ShowResponseErrorMessage(
//                            it.message, it.messageMar
//                        )
//                    )
//                }
//            }
//        } else if (response.code() == 422) {
//            Log.d("messgeboy", "message s ${response.body()}")
//            dashboardEventChannel.send(
//                DashboardEvent.ShowResponseErrorMessage(
//                    "Invalid IMEI No", "अवैध IMEI No"
//                )
//            )
//            performForcefullyLogout()
//        } else {
//            dashboardEventChannel.send(
//                DashboardEvent.ShowFailureMessage(
//                    response.code().toString()
//                )
//            )
//        }
//        dashboardEventChannel.send(DashboardEvent.EnableDutyToggle)
//        dashboardEventChannel.send(DashboardEvent.HideProgressBar)
//    }

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

    fun onSettingTeamFabMenuClicked() = viewModelScope.launch {
        dashboardEventChannel.send(DashboardEvent.ShowSettingTeamScreen)
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

    private val _memberIds = MutableLiveData<List<AvailableEmpItem>>()
    val memberIds: LiveData<List<AvailableEmpItem>> get() = _memberIds

    fun updateSelectedMemberIds(ids: List<AvailableEmpItem>) {
        _memberIds.value = ids
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
//                            if
                            //                            (!isVehicleScanOn) dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
//                            else
                            dashboardEventChannel.send(DashboardEvent.StartVehicleQrScanner)
                            // dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
                        }

                        "D" -> {

                            if (!isVehicleScanOn) dashboardEventChannel.send(DashboardEvent.GetVehiclesData)
                            else dashboardEventChannel.send(DashboardEvent.StartVehicleQrScanner)
                        }

                        else -> {
                            if (userDataStore.getUserEssentials.first().employeeType == "S" || userDataStore.getUserEssentials.first().employeeType == "L") {
                                if (isTeamSelected.value == true) {
                                    dashboardEventChannel.send(DashboardEvent.ShowLiquidEmployeeDialog)
                                } else {
                                    val userVehicleDetails = UserVehicleDetails(
                                        "1",
                                        "",
                                        "1"
                                    )

                                    val latitude = userLocationLiveData.value?.latitude
                                    val longitude = userLocationLiveData.value?.longitude
                                    val memberIds = memberIds.value
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

                                    if (inPunchRequest != null) {
                                        saveInPunchDetails(
                                            CommonUtils.APP_ID,
                                            CommonUtils.CONTENT_TYPE,
                                            batteryStatus,
                                            inPunchRequest,
                                            userVehicleDetails
                                        )
                                    }
                                }
                            } else {
                                val userVehicleDetails = UserVehicleDetails(
                                    "1",
                                    "",
                                    "1"
                                )

                                val latitude = userLocationLiveData.value?.latitude
                                val longitude = userLocationLiveData.value?.longitude
                                val memberIds = memberIds.value
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

                                if (inPunchRequest != null) {
                                    saveInPunchDetails(
                                        CommonUtils.APP_ID,
                                        CommonUtils.CONTENT_TYPE,
                                        batteryStatus,
                                        inPunchRequest,
                                        userVehicleDetails
                                    )
                                }
                            }
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
                    dashboardEventChannel.send(DashboardEvent.NavigateToSelectUlbScreen)

                }
            } else if (type == CommonUtils.CONFIRM_OFF_DUTY_DIALOG) {

                dashboardEventChannel.send(DashboardEvent.TurnDutyOff)
                dashboardEventChannel.send(DashboardEvent.DismissAlertDialogFrag)
            }
        }

    fun onLanguageDialogSubmitBtnClicked(appLanguage: AppLanguage) = viewModelScope.launch {
        languageDataStore.savePreferredLanguage(appLanguage)
        dashboardEventChannel.send(DashboardEvent.RestartDashboardActivity)
        dashboardEventChannel.send(DashboardEvent.RestartDashboardActivity)
        dashboardEventChannel.send(DashboardEvent.DismissLanguageDialog)
    }

    fun saveInPunchLiquid(
        userId: String?,
        batteryStatus: Int,
        memberUserIds: List<Int>,
        latitude: Double?,
        longitude: Double?,
        inPunchRequest: InPunchRequest,
        userVehicleDetails: UserVehicleDetails?
    ) {
        viewModelScope.launch {
            if (userId.isNullOrEmpty()) return@launch

            val userVehicleDetails = UserVehicleDetails("1", "", "1")

            val inPunchRequest = InPunchRequest(
                DateTimeUtils.getServerTime(),
                DateTimeUtils.getYyyyMMddDate(),
                latitude?.toString() ?: "0.0",
                longitude?.toString() ?: "0.0",
                userId,
                "1",
                "1",
                userDataStore.getUserEssentials.first().employeeType,
                "",
                memberUserIds
            )
            try {
                saveInPunchDetails(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    batteryStatus,
                    inPunchRequest,
                    userVehicleDetails
                )
            } catch (e: Exception) {
                Log.e("Exception", e.toString())
            }
        }
    }

    fun performForcefullyLogout() {
        viewModelScope.launch {
            val userDetails = userDataStore.getUserEssentials.first()
            tempUserDataStore.saveUserEssentials(
                UserEssentials(
                    userDetails.userId,
                    userDetails.employeeType,
                    userDetails.userTypeId
                )
            )
            userDataStore.clearUserDatastore()
            sessionDataStore.clearSessionDatastore()
            dashboardEventChannel.send(DashboardEvent.StopLocationTracking)
            dashboardEventChannel.send(DashboardEvent.NavigateToLoginScreen)
        }
    }

    fun clearAllDataNewUser() {
        viewModelScope.launch {
            archivedDao.deleteAllArchivedData()
            userTravelLocDao.deleteAllUserTravelLatLongs()
            nearestLatLngDao.deleteAllNearestHouses()
            garbageCollectionDao.deleteAllGarbageCollection()
        }
    }


    fun getDeviceId(context: Context) {
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        var deviceId: String? = CommonUtils.getAndroidId(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            deviceId = telephonyManager.deviceId
        }

        deviceIdCon = deviceId
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
        object NavigateToSelectUlbScreen : DashboardEvent()
        object ShowSettingScreen : DashboardEvent()
        object ShowSettingTeamScreen : DashboardEvent()
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
        object ShowLiquidEmployeeDialog : DashboardEvent()
        data class TeamON(val status: Boolean) : DashboardEvent()


    }
}

