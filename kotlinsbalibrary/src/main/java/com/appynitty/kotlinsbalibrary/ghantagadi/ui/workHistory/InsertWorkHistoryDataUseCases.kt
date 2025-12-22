package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import retrofit2.Response
import javax.inject.Inject

class InsertWorkHistoryDetailsUseCases @Inject constructor(  private val workHistoryDao: WorkHistoryDao,) {
  operator suspend fun invoke( response:  Response<List<WorkHistoryDetailsResponse>>,date: String){
        if (response.isSuccessful) {
            response.body()?.let {
                it.forEach {
                    workHistoryDao.insertWorkHistory(WorkHistoryData(Refid=it.Refid,time=it.time,
                        name=it.name,vehicleNumber=it.vehicleNumber, areaName = it.areaName,
                        type = it.type, isSynced = true, id = 0, date = date))
                }
            }
        }
    }
}
