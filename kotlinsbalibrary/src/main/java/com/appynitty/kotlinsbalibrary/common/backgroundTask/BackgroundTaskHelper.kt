package com.appynitty.kotlinsbalibrary.common.backgroundTask

import android.content.Context
import android.util.Log
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.dao.UserDetailsDao
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.TempUserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class BackgroundTaskHelper @Inject constructor(private val archivedDao: ArchivedDao,
                                               private val userTravelLocDao: UserTravelLocDao,
                                               private val nearestLatLngDao: NearestLatLngDao,
                                               private val garbageCollectionDao: GarbageCollectionDao,
                                               private val tempUserDataStore: TempUserDataStore,
                                               private val userDataStore: UserDataStore,
                                               private val userDetailsDao: UserDetailsDao,
                                               private val submitGarbageApiHelper: SubmitGarbageApiHelper,
                                               @ApplicationContext private val appContext: Context,
    ) {

    private var userTypeId: String? = null

    init {
        getUserDetailsFromRoom()
    }

   suspend fun submitOfflineData(){
        if (checkSameUserLogin()) {
            Log.d("tempId", "Temp id is ${checkSameUserLogin()}")

            val gcCount = getGcCount()
            if (gcCount > 0) {
                userTypeId?.let {
                    submitGarbageApiHelper.saveGarbageCollectionOfflineDataToApi(
                        CommonUtils.APP_ID,
                        it,
                        CommonUtils.getBatteryStatus(appContext),
                        CommonUtils.CONTENT_TYPE,
                    )
                }
            }
        } else {
           clearAllDataNewUser()
        }
    }

    suspend fun getGcCount(): Int {
        return garbageCollectionDao.getRowCount()
    }
    suspend fun checkSameUserLogin(): Boolean {
        val tempUser = tempUserDataStore.getUserEssentials.first()
        val user = userDataStore.getUserEssentials.first()

        val tempId = tempUser.userId
        val userId = user.userId

        // Return true if IDs are same OR either is empty/null
        return tempId.isNullOrEmpty() || tempId == userId
    }

   suspend fun clearAllDataNewUser() {
            archivedDao.deleteAllArchivedData()
            userTravelLocDao.deleteAllUserTravelLatLongs()
            nearestLatLngDao.deleteAllNearestHouses()
            garbageCollectionDao.deleteAllGarbageCollection()
    }

    private fun getUserDetailsFromRoom() {
        /**
         * The viewmodel is created lazily upon access, and creation of the viewmodel must be done on the main thread.
         * Simply trying to access the viewmodel on the main thread before accessing it on the lifecycle io scope
         */  CoroutineScope(Dispatchers.IO).launch {
            val userDataFlow = userDetailsDao.gerUserData()

            val userData1 = userDataFlow.first()

            if (userData1 != null) {
                userTypeId = userData1.userTypeId
            }
        }
        }


}