package com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpGcRepository

class EmpGcViewModelFactory(
    private val application: Application,
    private val empGcDao: EmpGcDao,
    private val repository: EmpGcRepository,
    private val archivedDao: ArchivedDao,
    private val houseOnMapDao: EmpHouseOnMapDao,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmpSyncGcViewModel(application, empGcDao, repository, archivedDao,houseOnMapDao) as T
    }
}