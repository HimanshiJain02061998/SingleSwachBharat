package com.appynitty.kotlinsbalibrary.common.backgroundTask

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import com.appynitty.kotlinsbalibrary.common.location.updateImeiChange
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedData
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryNotSyncedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryNewDataTemp
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.GarbageCollectionResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class SubmitGarbageApiHelper @Inject constructor( private val garbageCollectionDao: GarbageCollectionDao,
                                                  private val garbageCollectionRepo: GarbageCollectionRepo,
                                                  private val archivedDao: ArchivedDao,
                                                  @ApplicationContext private val appContext: Context,
                                                  private val workHistoryNotSyncedDao: WorkHistoryNotSyncedDao
    ) {

    private val deleteImageList = ArrayList<String>()
    private var deviceIdCon: String? = null

    init {
        getDeviceId(appContext)
    }
    suspend fun saveGarbageCollectionOfflineDataToApi(
        appId: String,
        typeId: String,
        batteryStatus: Int,
        contentType: String,
    )  {

        val garbageCollectionDataList: List<GarbageCollectionData> =
            garbageCollectionDao.getGarbageCollectionDataByLimit(10, 0)

        prepareOfflineImages(garbageCollectionDataList)

        if (garbageCollectionDataList.isNotEmpty()) {

            try {

                val response = garbageCollectionRepo.saveGarbageCollectionOfflineData(
                    appId,
                    typeId,
                    batteryStatus,
                    contentType,
                    deviceIdCon,
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

                    }

                    else -> {

                    }
                }
            }
        } else {

//            syncDumpYardTrip()
        }
    }

    private suspend fun handleGarbageCollectionResponse(
        appId: String, typeId: String, batteryStatus: Int, contentType: String,
        response: Response<List<GarbageCollectionResponse>>
    ) {

        if (response.isSuccessful) {

            response.body()?.let {



                    it.forEach { garbageCollectionResponse ->

                        if (garbageCollectionResponse.status == CommonUtils.STATUS_SUCCESS) {

                            workHistoryNotSyncedDao.insertWorkHistoryNotSynced(WorkHistoryNewDataTemp(id=0, Refid = garbageCollectionResponse.referenceID))


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
        } else if(response.code()==422){
            updateImeiChange(true)
//                garbageCollectionChannel.send(LogoutEvent.ShowResponseErrorMessage("Invalid IMEI No", "अवैध IMEI No"))
//                garbageCollectionChannel.send(LogoutEvent.PerformForcefullyLogout)

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

    private suspend fun deleteGcById(offlineId: String) {
        garbageCollectionDao.deleteGCById(
            offlineId
        )
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

    fun getDeviceId(context: Context){
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        var deviceId: String? = CommonUtils.getAndroidId(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            deviceId = telephonyManager.deviceId
        }

        deviceIdCon  = deviceId
    }
}