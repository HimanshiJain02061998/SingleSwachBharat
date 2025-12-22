package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryNotSyncedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import retrofit2.Response
import javax.inject.Inject

class InsertWorkHistoryDetailsUseCases @Inject constructor(private val workHistoryDao: WorkHistoryDao, private val workHistoryNotSyncedDao: WorkHistoryNotSyncedDao) {
  operator suspend fun invoke( response:  Response<List<WorkHistoryDetailsResponse>>,date: String){
        if (response.isSuccessful) {
            response.body()?.let {
             val workHistoryList = it
                when {
                    workHistoryDao.getWorkHistoryCountForDate(date)==0 -> {
                        it.forEach {
                            workHistoryDao.insertWorkHistory(WorkHistoryData(Refid=it.Refid,time=it.time,
                                name=it.name,vehicleNumber=it.vehicleNumber, areaName = it.areaName,
                                type = it.type, isSynced = true, id = 0, date = date))
                        }
                    }

                    workHistoryDao.getWorkHistoryCountForDate(date)>0 && workHistoryDao.getWorkHistoryCountForDate(date)<it.size -> {

                        if (workHistoryNotSyncedDao.getTotalCount()>0){
                            workHistoryNotSyncedDao.getWorkHistoryNotSynced().forEach {
                                val item = workHistoryList.find { it.Refid == "abs" }
                                workHistoryDao.insertWorkHistory(WorkHistoryData(Refid=item?.Refid,time=item?.time,
                                    name=item?.name,vehicleNumber=item?.vehicleNumber, areaName = item?.areaName,
                                    type = item?.type, isSynced = true, id = 0, date = date))
                                workHistoryNotSyncedDao.deleteByRefId(it.Refid)
                            }
                        }else if (workHistoryDao.getWorkHistoryCountForDate(date)!=it.size){

                            it.forEach {
                                workHistoryDao.deleteByDateAndRefId(date,it.Refid)
                                workHistoryDao.insertWorkHistory(WorkHistoryData(Refid=it.Refid,time=it.time,
                                    name=it.name,vehicleNumber=it.vehicleNumber, areaName = it.areaName,
                                    type = it.type, isSynced = true, id = 0, date = date))
                            }
                        }

                    }
                }
            }
        }
    }
}
