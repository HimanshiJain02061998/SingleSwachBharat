package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import com.appynitty.kotlinsbalibrary.ghantagadi.dao.WorkHistoryDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.WorkHistoryData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryResponse
import retrofit2.Response
import javax.inject.Inject

class InsertWorkHistoryDataUseCases @Inject constructor(private val workHistoryDao: WorkHistoryDao) {
    operator suspend fun invoke(response:   Response<List<WorkHistoryResponse>>, year: String,month: String){
        if (response.isSuccessful) {
            response.body()?.let {
                val workHistoryList = it

                workHistoryList.forEach {
                    val entity = it?.date?.let { date ->
                        WorkHistoryData(
                            date = date,
                            houseCollection = it.houseCollection,
                            LiquidCollection = it.LiquidCollection,
                            StreetCollection = it.StreetCollection,
                            DumpYardCollection = it.DumpYardCollection,
                            DumpYardPlantCollection = it.DumpYardPlantCollection,
                            year = year,
                            month = month
                        )
                    }
                    entity?.let { data -> workHistoryDao.upsertWorkHistory(data) }
                }

            }
        }
    }
}
