package com.appynitty.kotlinsbalibrary.housescanify.ui.masterPlateActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpGcResponse
import com.appynitty.kotlinsbalibrary.housescanify.model.response.HouseIdExistsResponse
import com.appynitty.kotlinsbalibrary.housescanify.repository.MasterPlateRepository
import com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner.EmpQrViewModel.EmpQrEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MasterPlateViewModel @Inject constructor(
    private val masterPlateRepository: MasterPlateRepository
) : ViewModel() {
    private val masterPlateActivityEventChannel = Channel<MasterPlateActivityEvents>()
    val masterPlateActivityEventsFlow = masterPlateActivityEventChannel.receiveAsFlow()

    fun saveMasterPlateCollectionOnline(
        appId: String,
        contentType: String,
        masterPlateData: EmpGarbageCollectionRequest,
        qrImageFilePath: String?,
        propertyImageFilePath: String?
    ) = viewModelScope.launch {
        masterPlateActivityEventChannel.send(MasterPlateActivityEvents.ShowLoading)
        try {
            val response = masterPlateRepository.saveMasterPlateCollectionOnline(
                appId,
                contentType,
                masterPlateData
            )
            handleMasterPlateResponse(
                response, qrImageFilePath, propertyImageFilePath
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

    private fun handleMasterPlateResponse(
        response: Response<EmpGcResponse>,
        qrImageFilePath: String?,
        propertyImageFilePath: String?
    ) = viewModelScope.launch {
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.status == CommonUtils.STATUS_SUCCESS) {
                    masterPlateActivityEventChannel.send(
                        MasterPlateActivityEvents.ShowResponseSuccessMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                } else if (it.status == CommonUtils.STATUS_ERROR) {
                    masterPlateActivityEventChannel.send(
                        MasterPlateActivityEvents.ShowResponseErrorMessage(
                            it.message,
                            it.messageMar
                        )
                    )
                }
            }
        } else {
            EmpQrEvent.ShowFailureMessage(response.message())
        }
        masterPlateActivityEventChannel.send(
            MasterPlateActivityEvents.DeleteUploadedImages(
                qrImageFilePath,
                propertyImageFilePath
            )
        )
        masterPlateActivityEventChannel.send(MasterPlateActivityEvents.HideLoading)
        masterPlateActivityEventChannel.send(MasterPlateActivityEvents.FinishActivity)
    }

    fun checkHouseIdExistsOrNot(referenceId: String) = viewModelScope.launch {
        masterPlateActivityEventChannel.send(MasterPlateActivityEvents.ShowLoading)
        try {
            val response = masterPlateRepository.houseIdExists(CommonUtils.APP_ID, referenceId)
            handleHouseIdExistsResponse(response)
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    EmpQrEvent.ShowFailureMessage(
                        "Connection Timeout"
                    )

                else -> {
                    EmpQrEvent.ShowFailureMessage(
                        "Conversion Error"
                    )
                }
            }
        }
    }

    private fun handleHouseIdExistsResponse(response: Response<HouseIdExistsResponse>) =
        viewModelScope.launch {
            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.status == CommonUtils.STATUS_SUCCESS) {
                        masterPlateActivityEventChannel.send(
                            MasterPlateActivityEvents.HouseIdExists(
                                it.referenceID
                            )
                        )
                    } else if (it.status == CommonUtils.STATUS_ERROR) {
                        masterPlateActivityEventChannel.send(
                            MasterPlateActivityEvents.ShowResponseErrorMessage(
                                it.message,
                                it.messageMar
                            )
                        )
                    }
                }
            } else {
                masterPlateActivityEventChannel.send(
                    MasterPlateActivityEvents.ShowFailureMessage(
                        response.message()
                    )
                )
            }
            masterPlateActivityEventChannel.send(MasterPlateActivityEvents.HideLoading)
        }

    sealed class MasterPlateActivityEvents {
        data class ShowWarningMessage(val message: String) : MasterPlateActivityEvents()
        data class ShowFailureMessage(val message: String) : MasterPlateActivityEvents()

        data class ShowResponseSuccessMessage(val msg: String?, val msgMr: String?) :
            MasterPlateActivityEvents()

        data class ShowResponseErrorMessage(val msg: String?, val msgMr: String?) :
            MasterPlateActivityEvents()

        data class DeleteUploadedImages(
            val qrImagePath: String?,
            val propertyImagePath: String?
        ) :
            MasterPlateActivityEvents()

        data class HouseIdExists(val houseId: String) :
            MasterPlateActivityEvents()

        object ShowLoading : MasterPlateActivityEvents()
        object HideLoading : MasterPlateActivityEvents()
        object FinishActivity : MasterPlateActivityEvents()

    }
}