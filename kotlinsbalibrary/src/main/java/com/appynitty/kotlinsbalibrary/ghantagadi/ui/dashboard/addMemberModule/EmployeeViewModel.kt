package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject // ← import this

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository,
    private val userDataStore: UserDataStore,

    ) : ViewModel() {
    @Inject
    lateinit var sessionDataStore : SessionDataStore
    private val _employeeList = MutableLiveData<List<AvailableEmpItem>>()
    val employeeList: LiveData<List<AvailableEmpItem>> get() = _employeeList

    fun loadEmployees( ) {
        viewModelScope.launch {
            val employees = repository.fetchAvailableEmployees(CommonUtils.APP_ID, userDataStore.getUserEssentials.first().employeeType, userDataStore.getUserEssentials.first().userId)
            employees?.let {
                _employeeList.postValue(it)
            }
        }
    }
}
