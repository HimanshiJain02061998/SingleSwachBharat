package com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel  @Inject constructor(private val userDataStore: UserDataStore) : ViewModel() {

    val lastScannedHouseLatLong = userDataStore.getLastHouseScanifyLatLong.asLiveData()
    fun saveUserLocation(userLatLong: UserLatLong) = viewModelScope.launch {
        userDataStore.saveUserLatLong(userLatLong)
    }

}