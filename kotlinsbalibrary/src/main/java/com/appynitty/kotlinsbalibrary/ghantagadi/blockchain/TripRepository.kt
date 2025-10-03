package com.appynitty.kotlinsbalibrary.ghantagadi.blockchain


import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao.TripDao
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.dao.TripHouseDao
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripHouseData
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.model.TripRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.network.DumpYardTripApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val tripHouseDao: TripHouseDao,
    private val dumpYardTripApi: DumpYardTripApi
) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    suspend fun saveDumpYardTripToApi(
        appId: String,
        tripRequest: List<TripRequest>
    ) = dumpYardTripApi.syncDumpYardTrip(appId, tripRequest)

    fun saveDumpYardTripToRoom(
        tripRequest: TripRequest
    ) {
        scope.launch {
            tripDao.insert(tripRequest)
        }
    }

    fun deleteDumpYardTripFromRoom(offlineId: Int) {
        scope.launch {
            tripDao.deleteSingleTrip(offlineId)
        }
    }

    fun getAllDumpYardTripsFromRoom() = tripDao.getAllTrips()

    fun saveTripHouseToRoom(
        tripHouseData: TripHouseData
    ) {
        scope.launch {
            tripHouseDao.insert(tripHouseData)
        }
    }

    fun deleteAllTripHousesFromRoom() {
        scope.launch {
            tripHouseDao.deleteScannedHouse()
        }
    }

    fun getAllTripHousesFromRoom() = tripHouseDao.getAllScannedHouse()

    fun deleteTripHouseById(id: Int) = scope.launch {
        tripHouseDao.deleteById(id)
    }
}