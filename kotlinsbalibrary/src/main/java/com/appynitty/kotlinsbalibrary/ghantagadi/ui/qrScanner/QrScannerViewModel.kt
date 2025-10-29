package com.appynitty.kotlinsbalibrary.ghantagadi.ui.qrScanner

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.repository.NearestLatLngRepository
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.TripRepository
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripHouseData
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.GarbageCollectionResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

private const val TAG = "QrScannerViewModel"

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val garbageCollectionRepo: GarbageCollectionRepo,
    private val garbageCollectionDao: GarbageCollectionDao,
    private val tripRepository: TripRepository,
    private val sessionDataStore: SessionDataStore,
    userDataStore: UserDataStore,
    private val archivedDao: ArchivedDao,
    private val userTravelLocDao: UserTravelLocDao
) : ViewModel() {

    @Inject
    lateinit var nearestLatLngRepository: NearestLatLngRepository

    @Inject
    lateinit var nearestLatLngDao: NearestLatLngDao

    @Inject
    lateinit var userDataStore: UserDataStore


    private val qrScannerEventChannel = Channel<QrScannerEvent>()
    val qrScannerEventsFlow = qrScannerEventChannel.receiveAsFlow()

    var gcType = ""
    var referenceId = ""
    var isGtFeatureOn: Boolean = false
    var submitDialogTitleText = ""
    var isInternetOn = false
    val userLatLongFlow = userDataStore.getUserLatLong
    private var deviceIdCon: String? = null

    fun validateScannedQrCode(empType: String, result: String) = viewModelScope.launch {
        if (result.length >= 5) {
            referenceId = result

            qrScannerEventChannel.send(QrScannerEvent.PauseScanner)
            if (referenceId.substring(0, 2).matches("^[DdYy]+\$".toRegex())) {
                if (empType != "D") {

                    gcType = "3"
                    submitDialogTitleText = "Dump yard Id"

                    // qrScannerEventChannel.send(QrScannerEvent.OpenDumpYardWeightActivityForResults)
                    qrScannerEventChannel.send(
                        QrScannerEvent.SubmitScanQrData(
                            null, null, true
                        )
                    )


                } else {
                    //handle if emp type (d) functionality needs to implement
                    qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.qr_error))

                }
            } else {
                when (empType) {
                    "N" -> {
                        if (referenceId.substring(0, 2).matches("^[HhPp]+\$".toRegex())) {
                            gcType = "1"
                            submitDialogTitleText = "House Id"
                            if (isGtFeatureOn) {
                                qrScannerEventChannel.send(QrScannerEvent.ShowGarbageTypeDialog)
                            } else {
                                qrScannerEventChannel.send(
                                    QrScannerEvent.SubmitScanQrData(
                                        "3", null
                                    )
                                )
                                qrScannerEventChannel.send(QrScannerEvent.ClearImagePathFromDataStore)
                            }
                        } else if (referenceId.matches("^[Mm]-\\d{1,6}\$".toRegex())) {
                            gcType = "12"
                            submitDialogTitleText = "Master Plate Id"
                            if (isGtFeatureOn) {
                                qrScannerEventChannel.send(QrScannerEvent.ShowGarbageTypeDialog)
                            } else {
                                qrScannerEventChannel.send(
                                    QrScannerEvent.SubmitScanQrData(
                                        "3", null
                                    )
                                )
                                qrScannerEventChannel.send(QrScannerEvent.ClearImagePathFromDataStore)
                            }
                        } else if (referenceId.substring(0, 2).matches("^[LlWw]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.lwc_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[Ss]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.ssc_qr_warning))
                        } else {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.qr_error))
                        }
                    }

                    "L" -> {
                        if (referenceId.substring(0, 2).matches("^[LlWw]+\$".toRegex())) {
                            gcType = "4"
                            submitDialogTitleText = "Liquid waste Id"
                            qrScannerEventChannel.send(
                                QrScannerEvent.SubmitScanQrData(
                                    null, null
                                )
                            )
                            qrScannerEventChannel.send(QrScannerEvent.ClearImagePathFromDataStore)
                        } else if (referenceId.substring(0, 2).matches("^[HhPp]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.house_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[Ss]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.ssc_qr_warning))
                        } else {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.qr_error))
                        }
                    }

                    null -> {
                        Log.i(TAG, "validateScannedQrCode: null emp type")
                    }

                    "S" -> {
                        if (referenceId.substring(0, 2).matches("^[Ss]+\$".toRegex())) {
                            gcType = "5"
                            submitDialogTitleText = "Street waste Id"
                            qrScannerEventChannel.send(
                                QrScannerEvent.SubmitScanQrData(
                                    null, null
                                )
                            )
                            qrScannerEventChannel.send(QrScannerEvent.ClearImagePathFromDataStore)

                        } else if (referenceId.substring(0, 2).matches("^[LlWw]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.lwc_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[HhPp]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.house_qr_alert))
                        } else {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.qr_error))
                        }
                    }

                    "D" -> {
//                        if (referenceId.substring(0, 2).matches("^[VvQqRr]+\$".toRegex())) {
//                            gcType = "6"
//                            submitDialogTitleText = "Dump yard Id"
//                        } else
                        if (referenceId.substring(0, 2).matches("^[HhPp]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.house_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[DdYy]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.dy_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[LlWw]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.lwc_qr_alert))
                        } else if (referenceId.substring(0, 2).matches("^[Ss]+\$".toRegex())) {
                            qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.ssc_qr_warning))
                        } else {
                            gcType = "6"
                            submitDialogTitleText = "Vehicle Id"
                            qrScannerEventChannel.send(QrScannerEvent.OpenDumpYardWeightActivityForResults)
                        }
                    }

                    else -> {
                        qrScannerEventChannel.send(QrScannerEvent.ShowAlertDialog(R.string.qr_error))
                    }
                }
            }
        } else {
            qrScannerEventChannel.send(QrScannerEvent.ShowWarningMessage(R.string.qr_error))
            qrScannerEventChannel.send(QrScannerEvent.ResumeScanner)
        }
    }

    fun garbageTypeDialogSubmitClicked(garbageType: String?, note: String?) =
        viewModelScope.launch {
            if (garbageType == null) {
                qrScannerEventChannel.send(QrScannerEvent.ShowWarningMessage(R.string.select_garbage_type))
            } else {
                qrScannerEventChannel.send(QrScannerEvent.SubmitScanQrData(garbageType, note))
                qrScannerEventChannel.send(QrScannerEvent.ClearImagePathFromDataStore)
            }
        }

    fun saveGarbageCollectionOnlineDataToApi(
        appId: String,
        typeId: String,
        batteryStatus: Int,
        contentType: String,
        garbageCollectionData: GarbageCollectionData
    ) = viewModelScope.launch {

        qrScannerEventChannel.send(QrScannerEvent.ShowLoading)
        try {
            val response = garbageCollectionRepo.saveGarbageCollectionOnlineData(
                appId, typeId, batteryStatus, contentType,deviceIdCon, garbageCollectionData
            )

            handleGarbageCollectionResponse(
                response
            )

        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    QrScannerEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else -> QrScannerEvent.ShowFailureMessage(
                    "Conversion Error"
                )
            }
            qrScannerEventChannel.send(QrScannerEvent.FinishActivity)
        }
    }

    private fun handleGarbageCollectionResponse(
        response: Response<List<GarbageCollectionResponse>>
    ) = viewModelScope.launch {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it[0].status != null) {

                    if (it[0].status == CommonUtils.STATUS_SUCCESS) {
                        qrScannerEventChannel.send(
                            QrScannerEvent.ShowResponseSuccessMessage(
                                it[0].message, it[0].messageMar
                            )
                        )
                        qrScannerEventChannel.send(QrScannerEvent.ShowSuccessDialog(it[0].referenceID))
                        qrScannerEventChannel.send(QrScannerEvent.DeleteImages)

                    } else {
                        qrScannerEventChannel.send(
                            QrScannerEvent.ShowResponseErrorMessage(
                                it[0].message, it[0].messageMar
                            )
                        )
                        qrScannerEventChannel.send(QrScannerEvent.FinishActivity)
                    }
                } else {
                    // may be needed  condition in future for nearby scan functionality
                    qrScannerEventChannel.send(QrScannerEvent.FinishActivity)
                    qrScannerEventChannel.send(
                        QrScannerEvent.ShowResponseErrorMessage(
                            it[0].message,
                            it[0].messageMar
                        )
                    )
                }
            }
        } else if(response.code()==422){
//            qrScannerEventChannel.send(
//                QrScannerEvent.ShowResponseSuccessMessage(
//                    response.body()?.,  response.body()?.messageMar
//                )
//            )
            qrScannerEventChannel.send(
                QrScannerEvent.ShowResponseErrorMessage(
                    "Invalid IMEI No", "अवैध IMEI No"
                )
            )
            performForcefullyLogout()
        }
        else {
            qrScannerEventChannel.send(QrScannerEvent.ShowFailureMessage(response.message()))
            qrScannerEventChannel.send(QrScannerEvent.FinishActivity)
        }
        qrScannerEventChannel.send(QrScannerEvent.HideLoading)
    }


    fun saveGarbageCollectionOffline(garbageCollectionData: GarbageCollectionData) =
        viewModelScope.launch {

            val tempList = garbageCollectionDao.getGarbageCollectionData().first()
            if (tempList.isNotEmpty()) {
                tempList.forEach {
                    if (garbageCollectionData.referenceId == it.referenceId) {
                        garbageCollectionDao.deleteGCById(it.offlineId.toString())
                    }
                }
            }
            garbageCollectionDao.insertGarbageCollection(garbageCollectionData)
            qrScannerEventChannel.send(QrScannerEvent.ShowSuccessToast(R.string.saved_offline))
            qrScannerEventChannel.send(
                QrScannerEvent.FinishActivity
            )
        }

    fun insertTripHouse(garbageType: String) = viewModelScope.launch {
        if (gcType != "3" && gcType == "1") {

            if (garbageType == "2") {
                val tempList = tripRepository.getAllTripHousesFromRoom().first()
                tempList.forEach {
                    if (referenceId == it.houseId) {
                        tripRepository.deleteTripHouseById(it.id)
                    }
                }
            } else {
                val tempList = tripRepository.getAllTripHousesFromRoom().first()
                if (tempList.isNotEmpty()) {
                    tempList.forEach {
                        if (it.houseId == referenceId) {
                            tripRepository.deleteTripHouseById(it.id)
                        }
                    }
                }
                val startDateTime = DateTimeUtils.getScanningServerDate()
                val tripHouseData = TripHouseData(0, referenceId, startDateTime)
                tripRepository.saveTripHouseToRoom(tripHouseData)
            }


        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun saveDumpYardTrip(
        wetWeight: Double,
        dryWeight: Double,
        totalWeight: Double,
        userId: String,
        vehicleNumber: String
    ) = GlobalScope.launch(Dispatchers.IO) {

        val tripNo = sessionDataStore.getDumpYardTripNo.first() + 1
        val tempList = tripRepository.getAllTripHousesFromRoom().first()

        if (tempList.isNotEmpty()) {

            val houseList = ArrayList<String>()
            tempList.forEach {
                houseList.add(it.houseId)
            }

            Log.d(TAG, "saveDumpYardTrip: $tempList")
            if (houseList.isNotEmpty()) {

                val transId: String =
                    (CommonUtils.APP_ID + "&" + userId + "&" + DateTimeUtils.getScanningServerDate() + "&" + referenceId + "&" + tripNo)

                val tripRequest: TripRequest
                if (tempList[0].startDateTime != null) {

                    withContext(Dispatchers.Main) {

                        tripRequest = TripRequest(
                            0,
                            transId,
                            tempList[0].startDateTime!!,
                            DateTimeUtils.getScanningServerDate(),
                            userId,
                            referenceId,
                            houseList,
                            tripNo,
                            vehicleNumber,
                            wetWeight,
                            dryWeight,
                            totalWeight,
                            tempList.size
                        )
                    }

                    tripRepository.deleteAllTripHousesFromRoom()
                    sessionDataStore.saveDumpYardTripNo(tripNo)

                    if (isInternetOn) {

                        val tempList1 = ArrayList<TripRequest>()
                        tempList1.add(tripRequest)

                        try {
                            val response = tripRepository.saveDumpYardTripToApi(
                                CommonUtils.APP_ID, tempList1
                            )
                            handleDumpTripResponse(response, tripRequest)
                        } catch (t: Throwable) {
                            when (t) {
                                is IOException -> Log.e(TAG, "Dump Trip Network Failure")
                                else -> Log.e(TAG, t.message.toString())
                            }
                        }
                    } else {
                        tripRepository.saveDumpYardTripToRoom(tripRequest)
                    }

                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleDumpTripResponse(
        response: Response<List<TripResponse>>,
        tripRequest: TripRequest
    ) =
        GlobalScope.launch(Dispatchers.IO) {
            if (response.isSuccessful) {
                if (response.body() != null) {

                }
            } else {
                tripRepository.saveDumpYardTripToRoom(tripRequest)
            }
        }

    fun saveBeforeImagePath(filePath: String) = viewModelScope.launch {
        sessionDataStore.saveBeforeImageFilePath(filePath)
    }

    fun getDeviceId(context: Context){
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        var deviceId: String? = CommonUtils.getAndroidId(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            deviceId = telephonyManager.deviceId
        }

        deviceIdCon  = deviceId
    }

    private fun performForcefullyLogout(){
        viewModelScope.launch {
            userDataStore.clearUserDatastore()
            sessionDataStore.clearSessionDatastore()
//            archivedDao.deleteAllArchivedData()
//            userTravelLocDao.deleteAllUserTravelLatLongs()
//            nearestLatLngDao.deleteAllNearestHouses()
//            garbageCollectionDao.deleteAllGarbageCollection()
            qrScannerEventChannel.send(QrScannerEvent.NavigateToLoginScreen)
        }
    }

    sealed class QrScannerEvent {

        data class ShowAlertDialog(val resourceId: Int) : QrScannerEvent()
        data class ShowWarningMessage(val resourceId: Int) : QrScannerEvent()
        object ShowGarbageTypeDialog : QrScannerEvent()
        object ResumeScanner : QrScannerEvent()
        object PauseScanner : QrScannerEvent()
        object ClearImagePathFromDataStore : QrScannerEvent()
        object OpenDumpYardWeightActivityForResults : QrScannerEvent()
        data class SubmitScanQrData(
            val garbageType: String?,
            val note: String?,
            val isDumpDirectSubmit: Boolean = false
        ) : QrScannerEvent()

        object ShowLoading : QrScannerEvent()
        object HideLoading : QrScannerEvent()
        object DeleteImages : QrScannerEvent()
        object FinishActivity : QrScannerEvent()
        data class ShowSuccessDialog(val referenceId: String?) : QrScannerEvent()
        data class ShowFailureMessage(val msg: String) : QrScannerEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            QrScannerEvent()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : QrScannerEvent()
        data class ShowSuccessToast(val resourceId: Int) : QrScannerEvent()
        object NavigateToLoginScreen : QrScannerEvent()

    }
}