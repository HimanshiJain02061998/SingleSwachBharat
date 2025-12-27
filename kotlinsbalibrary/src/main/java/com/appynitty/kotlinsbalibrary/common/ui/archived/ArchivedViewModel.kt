package com.appynitty.kotlinsbalibrary.common.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val archivedDao: ArchivedDao,
    private val userDataStore: UserDataStore,
) : ViewModel() {

    val archivedLiveData = archivedDao.getArchivedData()


    init {
        setArchivedDataCount()
    }

    private fun setArchivedDataCount() {
        viewModelScope.launch {
            userDataStore.saveArchivedDataCount(0)
        }
    }

}