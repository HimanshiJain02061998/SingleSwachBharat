package com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.TripRepository
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDaoTemp
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo

class GarbageCollectionViewModelFactory(
    private val application: Application,
    private val repository: GarbageCollectionRepo,
    private val garbageCollectionDao: GarbageCollectionDao,
    private val garbageCollectionDaoTemp: GarbageCollectionDaoTemp,
    private val archivedDao: ArchivedDao,
    private val tripRepository: TripRepository,
    private val sessionDataStore: SessionDataStore,
    private val userDataStore: UserDataStore,

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GarbageCollectionViewModel(
            application, repository, garbageCollectionDao,garbageCollectionDaoTemp, archivedDao,
            tripRepository, sessionDataStore,userDataStore
        ) as T
    }

}