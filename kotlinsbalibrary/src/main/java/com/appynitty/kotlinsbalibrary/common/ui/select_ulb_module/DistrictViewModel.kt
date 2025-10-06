package com.appynitty.kotlinsbalibrary.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.model.response.District
import com.appynitty.kotlinsbalibrary.common.model.response.ULB
import com.appynitty.kotlinsbalibrary.common.repository.DistrictRepository
import com.appynitty.kotlinsbalibrary.common.repository.ULBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DistrictViewModel @Inject constructor(
    private val repository: DistrictRepository,
    private val ulbRepository: ULBRepository
) : ViewModel() {
    private val _ulbList = MutableStateFlow<List<ULB>>(emptyList())
    val ulbList: StateFlow<List<ULB>> = _ulbList
    private val _districtList = MutableStateFlow<List<District>>(emptyList())
    val districtList: StateFlow<List<District>> = _districtList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchDistrictList() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getDistrictList()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _districtList.value = it.DistrictList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchULBList(disId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = ulbRepository.getULBList(disId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _ulbList.value = it.ulbList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
