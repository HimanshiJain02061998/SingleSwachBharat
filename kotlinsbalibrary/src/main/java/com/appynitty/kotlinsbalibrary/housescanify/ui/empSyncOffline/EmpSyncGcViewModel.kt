package com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedData
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils.Companion.SIMPLE_DATE_FORMAT
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpGcResponse
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpGcRepository
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EmpSyncGcViewModel(
    application: Application,
    private val empGcDao: EmpGcDao,
    private val empGcRepository: EmpGcRepository,
    private val archivedDao: ArchivedDao,
    private val houseOnMapDao: EmpHouseOnMapDao
) : AndroidViewModel(application) {

    private val deleteImageList = ArrayList<String>()

    val empGcOfflineResponseLiveData: MutableLiveData<ApiResponseListener<List<EmpGcResponse>>?> =
        MutableLiveData()

    val isSyncingOnLiveData = MutableLiveData<Boolean>()

    private fun setSyncingLiveDataToNull() {
        empGcOfflineResponseLiveData.postValue(null)
        isSyncingOnLiveData.postValue(false)
    }

    fun saveGarbageCollectionOfflineDataToApi(
        appId: String, contentType: String
    ) = viewModelScope.launch {

        val garbageCollectionDataList: List<EmpGarbageCollectionRequest> =
            empGcDao.getGarbageCollectionDataByLimit(10, 0)

        prepareOfflineImages(garbageCollectionDataList)

        if (garbageCollectionDataList.isNotEmpty()) {
            empGcOfflineResponseLiveData.postValue(ApiResponseListener.Loading())
            try {
                isSyncingOnLiveData.postValue(true)

                val response = empGcRepository.saveGarbageCollectionOfflineData(
                    appId, contentType, garbageCollectionDataList
                )
                empGcOfflineResponseLiveData.postValue(
                    handleGarbageCollectionResponse(
                        appId, contentType, response
                    )
                )
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> {
                        empGcOfflineResponseLiveData.postValue(
                            ApiResponseListener.Failure(
                                "Connection Timeout"
                            )
                        )
                        setSyncingLiveDataToNull()
                    }
                    else -> {
                        empGcOfflineResponseLiveData.postValue(
                            ApiResponseListener.Failure(
                                "Conversion Error"
                            )
                        )
                        setSyncingLiveDataToNull()
                    }
                }
            }
        } else {
            setSyncingLiveDataToNull()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleGarbageCollectionResponse(
        appId: String, contentType: String, response: Response<List<EmpGcResponse>>
    ): ApiResponseListener<List<EmpGcResponse>> {
        if (response.isSuccessful) {

            response.body()?.let {

                viewModelScope.launch {
                    it.forEach { garbageCollectionResponse ->

                        if (garbageCollectionResponse.status == CommonUtils.STATUS_SUCCESS) {

                            //TODO - should be taken care of

                        } else if (garbageCollectionResponse.status == CommonUtils.STATUS_ERROR) {

                            val archivedData = ArchivedData(
                                0,
                                garbageCollectionResponse.referenceID,
                                garbageCollectionResponse.message,
                                garbageCollectionResponse.messageMar
                            )
                            archivedDao.insertArchivedData(archivedData)
                            garbageCollectionResponse.referenceID?.let { it1 ->
                                houseOnMapDao.deleteHouseOnMapById(
                                    it1
                                )
                            }
                        }

                        garbageCollectionResponse.offlineId?.let { it1 ->
                            empGcDao.deleteGCById(
                                it1
                            )
                        }
                    }
                    saveGarbageCollectionOfflineDataToApi(appId, contentType)
                    deleteUploadedImages()
                }
                return ApiResponseListener.Success(it)

            }
        }
        return ApiResponseListener.Failure(response.message())
    }


    private fun prepareOfflineImages(garbageCollectionDataList: List<EmpGarbageCollectionRequest>?) {
        if (!garbageCollectionDataList.isNullOrEmpty()) {

            garbageCollectionDataList.forEach {
                if ((!it.referenceImage.isNullOrEmpty() || !it.HouseImage.isNullOrEmpty())) {

                    val serverDateFormat =
                        SimpleDateFormat(DateTimeUtils.GIS_DATE_TIME_FORMAT, Locale.ENGLISH)
                    val dateFormat = SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH)

                    val gcDate: String = serverDateFormat.parse(it.date)
                        ?.let { it1 ->
                            dateFormat.format(it1).toString()
                        }.toString()

                    var qrBase64Image = ""
                    if (!it.referenceImage.isNullOrEmpty()) {
                        val qrBitmap = BitmapFactory.decodeFile(it.referenceImage)
                        if (qrBitmap != null){
                            qrBase64Image = CameraUtils.prepareBase64Images(
                                qrBitmap,
                                it.referenceId,
                                it.referenceImage!!,
                                it.latitude,
                                it.longitude,
                                gcDate
                            )
                        }
                    }
                    var propertyBase64Image = ""
                    if (!it.HouseImage.isNullOrEmpty()) {
                        val propertyBitmap = BitmapFactory.decodeFile(it.HouseImage)
                        if (propertyBitmap != null){
                            propertyBase64Image = CameraUtils.prepareBase64Images(
                                propertyBitmap,
                                it.referenceId,
                                it.HouseImage!!,
                                it.latitude,
                                it.longitude,
                                gcDate
                            )
                        }
                    }

                    it.referenceImage = qrBase64Image
                    it.referenceImage?.let { it1 -> deleteImageList.add(it1) }
                    it.HouseImage = propertyBase64Image
                    it.HouseImage?.let { it1 -> deleteImageList.add(it1) }
                }
            }
        }
    }

    private fun deleteUploadedImages() {

        val tempList = ArrayList<String>()
        tempList.addAll(deleteImageList)

        if (tempList.isNotEmpty()) {
            tempList.forEach {
                CameraUtils.deleteTheFile(it)
            }
            deleteImageList.clear()
        }

    }

    fun getGarbageCollectionListFromRoom() = empGcDao.getAllEmpGcData()

    suspend fun getGcCount(): Int {

        val job = viewModelScope.async {
            return@async empGcDao.getRowCount()
        }
        return job.await()
    }

}