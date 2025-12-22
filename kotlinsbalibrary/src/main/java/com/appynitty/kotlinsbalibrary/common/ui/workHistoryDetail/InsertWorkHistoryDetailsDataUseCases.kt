package com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail

import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDetailsDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryNotSyncedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryDetailsData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import retrofit2.Response
import javax.inject.Inject

class InsertWorkHistoryDetailsUseCases @Inject constructor(private val workHistoryDao: WorkHistoryDetailsDao, private val workHistoryNotSyncedDao: WorkHistoryNotSyncedDao) {
  operator suspend fun invoke( response:  Response<List<WorkHistoryDetailsResponse>>,date: String){
        if (response.isSuccessful) {
            response.body()?.let {
             val workHistoryList = it
                when {
                    workHistoryDao.getWorkHistoryDetailsCountForDate(date)==0 -> {
                        it.forEach {
                            workHistoryDao.insertWorkHistoryDetails(WorkHistoryDetailsData(Refid=it.Refid,time=it.time,
                                name=it.name,vehicleNumber=it.vehicleNumber, areaName = it.areaName,
                                type = it.type, isSynced = true, id = 0, date = date))
                        }
                    }

                    workHistoryDao.getWorkHistoryDetailsCountForDate(date)>0 && workHistoryDao.getWorkHistoryDetailsCountForDate(date)<it.size -> {

                        if (workHistoryNotSyncedDao.getTotalCount()>0){
                            workHistoryNotSyncedDao.getWorkHistoryNotSynced().forEach {
                                val item = workHistoryList.find { it.Refid == "abs" }
                                workHistoryDao.insertWorkHistoryDetails(WorkHistoryDetailsData(Refid=item?.Refid,time=item?.time,
                                    name=item?.name,vehicleNumber=item?.vehicleNumber, areaName = item?.areaName,
                                    type = item?.type, isSynced = true, id = 0, date = date))
                                workHistoryNotSyncedDao.deleteByRefId(it.Refid)
                            }
                        }else if (workHistoryDao.getWorkHistoryDetailsCountForDate(date)!=it.size){

                            it.forEach {
                                workHistoryDao.deleteByDateAndRefId(date,it.Refid)
                                workHistoryDao.insertWorkHistoryDetails(WorkHistoryDetailsData(Refid=it.Refid,time=it.time,
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
