package com.appynitty.kotlinsbalibrary.common.ui.archived

import androidx.lifecycle.ViewModel
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val archivedDao: ArchivedDao
) : ViewModel() {

    val archivedLiveData = archivedDao.getArchivedData()

}