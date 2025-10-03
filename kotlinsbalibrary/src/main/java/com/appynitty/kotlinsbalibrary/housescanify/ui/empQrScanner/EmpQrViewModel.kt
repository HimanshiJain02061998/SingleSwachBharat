package com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.PropertyTypeDao
import com.appynitty.kotlinsbalibrary.housescanify.model.EmpHouseOnMap
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpGcResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpHouseOnMapResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.MasterPlateExist
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpGcRepository
import com.appynitty.kotlinsbalibrary.housescanify.repository.MasterPlateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EmpQrViewModel @Inject constructor(
    private val houseOnMapDao: EmpHouseOnMapDao,
    private val empGcRepository: EmpGcRepository,
    private val masterPlateRepository: MasterPlateRepository,
    private val empGcDao: EmpGcDao,
    propertyTypeDao: PropertyTypeDao,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val empQrEventChannel = Channel<EmpQrEvent>()
    val empQrEventsFlow = empQrEventChannel.receiveAsFlow()

    val houseOnMapList = ArrayList<EmpHouseOnMapResponse>()
    var gcType = ""

    val propertyTypeList = propertyTypeDao.getAllPropertyTypes()

    fun getHouseOnMapHistory(
        appId: String, userId: String, date: String
    ) = viewModelScope.launch {

        empQrEventChannel.send(EmpQrEvent.ShowLoading)
        try {
            val response = empGcRepository.getHouseOnMapHistory(
                appId, userId, date
            )
            handleHouseOnMapResponse(response)

        } catch (t: Throwable) {
            getHouseOnMapHistoryFromRoom()

        }
    }

    private fun handleHouseOnMapResponse(response: Response<List<EmpHouseOnMapResponse>>) =
        viewModelScope.launch {

            if (response.isSuccessful) {
                response.body()?.let {
                    houseOnMapList.addAll(it)
                }
                if (houseOnMapList.isNotEmpty()) {
                    empQrEventChannel.send(EmpQrEvent.StartMapActivityForResult)
                } else {
                    getHouseOnMapHistoryFromRoom()
                }
            } else {
                getHouseOnMapHistoryFromRoom()
            }

        }

    fun getHouseOnMapHistoryFromRoom() = viewModelScope.launch {
        val tempList = houseOnMapDao.getAllHouseOnMapData().first()
        tempList.forEach {
            houseOnMapList.add(
                EmpHouseOnMapResponse(
                    it.referenceId,
                    it.latitude,
                    it.longitude,
                    it.gcType
                )
            )
        }
        empQrEventChannel.send(EmpQrEvent.StartMapActivityForResult)
    }

    fun insertHouseOnMap(houseOnMap: EmpHouseOnMap) =
        viewModelScope.launch(Dispatchers.IO) {
            houseOnMapDao.insertHouseOnMap(houseOnMap)
            userDataStore.saveLastHouseScanifyLatLong(
                UserLatLong(
                    houseOnMap.latitude.toString(),
                    houseOnMap.longitude.toString(),
                    "0"
                )
            )
        }

    fun validateScannedQrCode(referenceId: String) = viewModelScope.launch {
        if (referenceId.length >= 5) {
            if (referenceId.substring(0, 2).matches("^[HhPp]+\$".toRegex())) {
                gcType = "1"
                empQrEventChannel.send(EmpQrEvent.LoadHouseOnMapHistory)

            } else if (referenceId.substring(0, 2).matches("^[DdYy]+\$".toRegex())) {
                empQrEventChannel.send(EmpQrEvent.LoadHouseOnMapHistory)
                gcType = "3"
                // empQrEventChannel.send(EmpQrEvent.StartMapActivityForResult)
            } else if (referenceId.substring(0, 2).matches("^[LlWw]+\$".toRegex())) {
                empQrEventChannel.send(EmpQrEvent.LoadHouseOnMapHistory)
                gcType = "4"
                //  empQrEventChannel.send(EmpQrEvent.StartMapActivityForResult)
            } else if (referenceId.substring(0, 2).matches("^[Ss]+\$".toRegex())) {
                empQrEventChannel.send(EmpQrEvent.LoadHouseOnMapHistory)
                gcType = "5"
                //  empQrEventChannel.send(EmpQrEvent.StartMapActivityForResult)
            } else if (referenceId.matches("^[Mm]-\\d{1,6}\$".toRegex())) {
                doesMasterPlateExists(referenceId)
                gcType = "12"
            } else {
                empQrEventChannel.send(EmpQrEvent.ShowWarningMessage(R.string.qr_error))
                empQrEventChannel.send(EmpQrEvent.ResumeQrScanner)
            }
        } else {
            empQrEventChannel.send(EmpQrEvent.ShowWarningMessage(R.string.qr_error))
            empQrEventChannel.send(EmpQrEvent.ResumeQrScanner)
        }
    }

    private fun doesMasterPlateExists(referenceId: String) = viewModelScope.launch {
        empQrEventChannel.send(EmpQrEvent.ShowLoading)
        try {
            val response =
                masterPlateRepository.masterPlateExists(appId = CommonUtils.APP_ID, referenceId)
            handleMasterPlateResponse(response)
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    EmpQrEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else ->
                    EmpQrEvent.ShowFailureMessage(
                        "Conversion Error"
                    )
            }
        }
    }

    private fun handleMasterPlateResponse(response: Response<MasterPlateExist>) =
        viewModelScope.launch {

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.status == CommonUtils.STATUS_SUCCESS) {
                        if (it.isReScan) {
                            empQrEventChannel.send(
                                EmpQrEvent.NavigateToMasterPlateActivity(
                                    it.referenceID,
                                    it.bunchList
                                )
                            )
                            empQrEventChannel.send(EmpQrEvent.FinishActivity)
                        } else {
                            empQrEventChannel.send(EmpQrEvent.LoadHouseOnMapHistory)
                        }
                    } else if (it.status == CommonUtils.STATUS_ERROR) {
                        empQrEventChannel.send(
                            EmpQrEvent.ShowResponseErrorMessage(
                                it.message,
                                it.messageMar
                            )
                        )
                        empQrEventChannel.send(EmpQrEvent.FinishActivity)
                    }
                }
            } else {
                EmpQrEvent.ShowFailureMessage(response.message())
            }
            empQrEventChannel.send(EmpQrEvent.HideLoading)
        }

    fun saveGarbageCollectionOnlineDataToApi(
        appId: String,
        contentType: String,
        garbageCollectionData: EmpGarbageCollectionRequest,
        empHouseOnMap: EmpHouseOnMap,
        qrImageFilePath: String,
        propertyImageFilePath: String
    ) = viewModelScope.launch {

        empQrEventChannel.send(EmpQrEvent.ShowLoading)

        try {

            val response = empGcRepository.saveGarbageCollectionOnlineData(
                appId, contentType, garbageCollectionData
            )
            handleOnlineGarbageCollectionResponse(
                response, empHouseOnMap, qrImageFilePath, propertyImageFilePath
            )

        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    EmpQrEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else ->
                    EmpQrEvent.ShowFailureMessage(
                        "Conversion Error"
                    )
            }
        }
    }

    private fun handleOnlineGarbageCollectionResponse(
        response: Response<EmpGcResponse>, empHouseOnMap: EmpHouseOnMap, qrImageFilePath: String,
        propertyImageFilePath: String
    ) = viewModelScope.launch {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == CommonUtils.STATUS_SUCCESS) {

                    insertHouseOnMap(empHouseOnMap)
                    userDataStore.saveLastHouseScanifyLatLong(
                        UserLatLong(
                            empHouseOnMap.latitude.toString(),
                            empHouseOnMap.longitude.toString(),
                            "0"
                        )
                    )
                    empQrEventChannel.send(
                        EmpQrEvent.ShowResponseSuccessMessage(
                            it.message,
                            it.messageMar
                        )
                    )

                } else if (it.status == CommonUtils.STATUS_ERROR) {
                    empQrEventChannel.send(
                        EmpQrEvent.ShowResponseErrorMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                }
            }
        } else {
            EmpQrEvent.ShowFailureMessage(response.message())
        }
        empQrEventChannel.send(
            EmpQrEvent.DeleteUploadedImage(
                qrImageFilePath,
                propertyImageFilePath
            )
        )
        empQrEventChannel.send(EmpQrEvent.HideLoading)
        empQrEventChannel.send(EmpQrEvent.FinishActivity)
    }

    fun insertGarbageCollectionInRoom(empGarbageCollectionRequest: EmpGarbageCollectionRequest) =
        viewModelScope.launch {

            val tempList = empGcDao.getAllEmpGcData().first()
            if (tempList.isNotEmpty()) {
                tempList.forEach {
                    if (empGarbageCollectionRequest.referenceId == it.referenceId) {
                        deleteGcById(it.offlineId.toString())
                    }
                }
            }
            empGcDao.insertEmpGc(empGarbageCollectionRequest)
            empQrEventChannel.send(EmpQrEvent.ShowSuccessToast(R.string.saved_offline))
            empQrEventChannel.send(EmpQrEvent.FinishActivity)
        }

    private fun deleteGcById(offlineId: String) = viewModelScope.launch {
        empGcDao.deleteGCById(offlineId)
    }

    sealed class EmpQrEvent {
        data class ShowWarningMessage(val resourceId: Int) : EmpQrEvent()
        object StartMapActivityForResult : EmpQrEvent()
        object LoadHouseOnMapHistory : EmpQrEvent()
        object ResumeQrScanner : EmpQrEvent()
        object ShowLoading : EmpQrEvent()
        object HideLoading : EmpQrEvent()
        data class DeleteUploadedImage(val qrImagePath: String, val propertyImagePath: String) :
            EmpQrEvent()

        object FinishActivity : EmpQrEvent()
        data class ShowSuccessToast(val resourceId: Int) : EmpQrEvent()
        data class ShowFailureMessage(val msg: String) : EmpQrEvent()
        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            EmpQrEvent()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) : EmpQrEvent()
        data class NavigateToMasterPlateActivity(val referenceId: String, val housesList: String) :
            EmpQrEvent()
    }
}