package com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedData
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.TripRepository
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.GarbageCollectionResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "GarbageCollectionViewMo"

class GarbageCollectionViewModel(
    application: Application,
    private val garbageCollectionRepo: GarbageCollectionRepo,
    private val garbageCollectionDao: GarbageCollectionDao,
    private val archivedDao: ArchivedDao,
    private val tripRepository: TripRepository,
    private val sessionDataStore: SessionDataStore
) : AndroidViewModel(application) {

    val garbageCollectionResponseLiveData: MutableLiveData<ApiResponseListener<List<GarbageCollectionResponse>>?> =
        MutableLiveData()


    private val deleteImageList = ArrayList<String>()
    val isSyncingOnLiveData = MutableLiveData<Boolean>()
    private val isDumpTripSyncFlow = MutableStateFlow(false)
    private var isDumpTripSyncOn = false

    init {

        viewModelScope.launch {
            isDumpTripSyncFlow.collect {
                isDumpTripSyncOn = it
            }
        }

    }

    fun setSyncingLiveDataToNull() {
        garbageCollectionResponseLiveData.postValue(null)
        isSyncingOnLiveData.postValue(false)
    }

    fun saveGarbageCollectionOfflineDataToApi(
        appId: String,
        typeId: String,
        batteryStatus: Int,
        contentType: String,
    ) = viewModelScope.launch {

        val garbageCollectionDataList: List<GarbageCollectionData> =
            garbageCollectionDao.getGarbageCollectionDataByLimit(10, 0)

        prepareOfflineImages(garbageCollectionDataList)

        if (garbageCollectionDataList.isNotEmpty()) {

            garbageCollectionResponseLiveData.postValue(ApiResponseListener.Loading())
            isSyncingOnLiveData.postValue(true)

            try {

                val response = garbageCollectionRepo.saveGarbageCollectionOfflineData(
                    appId,
                    typeId,
                    batteryStatus,
                    contentType,
                    garbageCollectionDataList
                )

                handleGarbageCollectionResponse(
                    appId,
                    typeId,
                    batteryStatus,
                    contentType,
                    response
                )

            } catch (t: Throwable) {
                when (t) {
                    is IOException -> {
                        garbageCollectionResponseLiveData.postValue(
                            ApiResponseListener.Failure(
                                "Connection Timeout"
                            )
                        )
                        setSyncingLiveDataToNull()
                    }

                    else -> {
                        garbageCollectionResponseLiveData.postValue(
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
            syncDumpYardTrip()
        }
    }

    private fun prepareOfflineImages(garbageCollectionDataList: List<GarbageCollectionData>?) {
        if (!garbageCollectionDataList.isNullOrEmpty()) {

            garbageCollectionDataList.forEach {
                if ((it.gpBeforeImage != null && it.gpBeforeImage != "") || (it.gpAfterImage != null && it.gpAfterImage != "")) {

                    val serverDateFormat =
                        SimpleDateFormat(
                            DateTimeUtils.SERVER_DATE_TIME_FORMAT_LOCAL,
                            Locale.ENGLISH
                        )
                    val dateFormat =
                        SimpleDateFormat(DateTimeUtils.SIMPLE_DATE_FORMAT, Locale.ENGLISH)

                    val gcDate: String = serverDateFormat.parse(it.gcDate)
                        ?.let { it1 ->
                            dateFormat.format(it1).toString()
                        }.toString()

                    val beforeAfterImagesMap = CameraUtils.prepareBeforeAfterImages(
                        it.gpBeforeImage, it.gpAfterImage, it.referenceId,
                        it.latitude!!,
                        it.longitude!!, gcDate
                    )

                    var beforeImageBase64: String? = null
                    var afterImageBase64: String? = null

                    if (it.gpBeforeImage != null)
                        beforeImageBase64 = beforeAfterImagesMap["beforeImageBase64"]

                    if (it.gpAfterImage != null)
                        afterImageBase64 = beforeAfterImagesMap["afterImageBase64"]

                    it.gpBeforeImage = beforeImageBase64
                    it.gpAfterImage = afterImageBase64

                    if (it.gpBeforeImage != null && it.gpBeforeImage != "")
                        it.gpBeforeImage?.let { it1 -> deleteImageList.add(it1) }
                    if (it.gpAfterImage != null && it.gpAfterImage != "")
                        it.gpAfterImage?.let { it1 -> deleteImageList.add(it1) }
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

    private fun handleGarbageCollectionResponse(
        appId: String, typeId: String, batteryStatus: Int, contentType: String,
        response: Response<List<GarbageCollectionResponse>>
    ) {
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

                        }
                        garbageCollectionResponse.offlineId?.let { it1 ->
                            if (garbageCollectionResponse.referenceID != null)
                                deleteGcById(it1)
                        }
                    }
                    deleteUploadedImages()
                    saveGarbageCollectionOfflineDataToApi(appId, typeId, batteryStatus, contentType)

                }
                garbageCollectionResponseLiveData.postValue(ApiResponseListener.Success(it))
            }
        } else {
            garbageCollectionResponseLiveData.postValue(ApiResponseListener.Failure(response.message()))
        }
    }

    private suspend fun deleteGcById(offlineId: String) {
        garbageCollectionDao.deleteGCById(
            offlineId
        )
    }


    fun getGarbageCollectionListFromRoom() = garbageCollectionDao.getGarbageCollectionData()

    suspend fun getGcCount(): Int {

        val job = viewModelScope.async {
            return@async garbageCollectionDao.getRowCount()
        }
        return job.await()

    }

    //blockchain sync
    @OptIn(DelicateCoroutinesApi::class)
    fun syncDumpYardTrip() = GlobalScope.launch(Dispatchers.IO) {

        Log.i("dumpSyncingOn", "syncDumpYardTrip: $isDumpTripSyncOn")

        if (!isDumpTripSyncOn) {

            val houseList = tripRepository.getAllDumpYardTripsFromRoom().first()
            if (houseList.isNotEmpty()) {

                try {
                    isDumpTripSyncFlow.emit(true)

                    val response = tripRepository.saveDumpYardTripToApi(
                        CommonUtils.APP_ID, houseList
                    )
                    val tripNo = houseList.last().tripNo

                    handleDumpTripResponse(response, tripNo)
                } catch (t: Throwable) {
                    when (t) {
                        is IOException -> Log.e(
                            TAG,
                            "Dump Trip Network Failure"
                        )

                        else -> Log.e(
                            TAG,
                            t.message.toString()
                        )
                    }
                    isDumpTripSyncFlow.emit(false)

                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleDumpTripResponse(response: Response<List<TripResponse>>, tripNo: Int) =
        GlobalScope.launch(Dispatchers.IO) {
            if (response.isSuccessful) {
                if (response.body() != null) {
                    //tripRepository.deleteAllTripHousesFromRoom()
                   // sessionDataStore.saveDumpYardTripNo(tripNo)

                    response.body()!!.forEach {
                        it.offlineId?.let { it1 -> tripRepository.deleteDumpYardTripFromRoom(it1) }
                    }
                }
            }
            isDumpTripSyncFlow.emit(false)
        }


}