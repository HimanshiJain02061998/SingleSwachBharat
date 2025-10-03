package com.appynitty.kotlinsbalibrary.common.ui.my_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.dao.NearestLatLngDao
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.UserTravelLocDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyLocationViewModel @Inject constructor(
    private val userDataStore: UserDataStore,
//    nearestLatLngDao: NearestLatLngDao,
//    userTravelLocDao: UserTravelLocDao
) : ViewModel() {

    val userLatLong = userDataStore.getUserLatLong.asLiveData()
    fun saveUserLocation(userLatLong: UserLatLong) = viewModelScope.launch {
        userDataStore.saveUserLatLong(userLatLong)
    }

   // val nearestLatLngs = nearestLatLngDao.getNearestHouses().asLiveData()
   // val trailLatLongs = userTravelLocDao.getAllUserTravelLatLongs().asLiveData()
}