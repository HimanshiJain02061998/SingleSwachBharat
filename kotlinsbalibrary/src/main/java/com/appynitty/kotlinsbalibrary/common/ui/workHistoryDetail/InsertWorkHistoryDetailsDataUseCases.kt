package com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail

import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDetailsDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryNotSyncedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryDetailsData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import retrofit2.Response
import javax.inject.Inject

class InsertWorkHistoryDetailsUseCases @Inject constructor(private val workHistoryDao: WorkHistoryDetailsDao, private val workHistoryNotSyncedDao: WorkHistoryNotSyncedDao) {
  operator suspend fun invoke( response:  Response<List<WorkHistoryDetailsResponse>>,date: String){

      if (!response.isSuccessful) return
      val entities = response.body()
          ?.mapNotNull { dto ->
              dto.Refid?.let { refId ->
                  WorkHistoryDetailsData(
                      id = 0,                 // surrogate key
                      date = date,            // ✅ CORRECT
                      Refid = refId,           // ✅ CORRECT
                      time = dto.time,
                      name = dto.name,
                      vehicleNumber = dto.vehicleNumber,
                      areaName = dto.areaName,
                      type = dto.type,
                  )
              }
          }
          .orEmpty()
      if (entities.isNotEmpty()) {
          workHistoryDao.upsertWorkHistoryDetails(entities)
      }

    }
}
