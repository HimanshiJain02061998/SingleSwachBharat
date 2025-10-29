package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import com.appynitty.kotlinsbalibrary.databinding.ActivitySelectMembersBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectMembers : AppCompatActivity() {

    private lateinit var binding: ActivitySelectMembersBinding
    private val employeeViewModel: EmployeeViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: MemberAdapter
    private var userId: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    @Inject
    lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        userId = intent.getStringExtra("USER_ID") // ✅ same key used when passing
        latitude = intent.getDoubleExtra("latitude", 0.0).toString()
        longitude = intent.getDoubleExtra("longitude", 0.0).toString()


        binding.recyclerMembers.layoutManager = LinearLayoutManager(this)
        adapter = MemberAdapter(mutableListOf())
        binding.recyclerMembers.adapter = adapter


        employeeViewModel.employeeList.observe(this) { employees ->
            adapter.updateList(employees)
        }
        adapter.onSelectionChanged = { count ->
            binding.tvSelectedCount.text = "Selected: $count"
        }


        employeeViewModel.loadEmployees()

        binding.searchViewMembers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        latitude = intent.getDoubleExtra("latitude", 0.0).toString()
        longitude = intent.getDoubleExtra("longitude", 0.0).toString()

        binding.btnConfirmSelection.setOnClickListener {
            val selectedMembers = adapter.getSelectedMembers()
            if (selectedMembers.isEmpty()) {
                Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMemberIds = selectedMembers.mapNotNull { it.userid }

            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userVehicleDetails = UserVehicleDetails("1", "", "1")

            // Launch a coroutine to call suspend functions
            lifecycleScope.launch {
                // Safe call to suspend function
                val employeeType = userDataStore.getUserEssentials.first().employeeType

                val inPunchRequest = InPunchRequest(
                    DateTimeUtils.getServerTime(),
                    DateTimeUtils.getYyyyMMddDate(),
                    latitude.toString(),
                    longitude.toString(),
                    userId,
                    "1",
                    "1",
                    employeeType,
                    "",
                    selectedMemberIds
                )

                dashboardViewModel.saveInPunchLiquid(
                    userId = userId,
                    batteryStatus = CommonUtils.getBatteryStatus(application),
                    memberUserIds = selectedMemberIds,
                    latitude = latitude?.toDouble(),
                    longitude = longitude?.toDouble(),
                    inPunchRequest = inPunchRequest,
                    userVehicleDetails = userVehicleDetails
                )
                val resultIntent = Intent()
                resultIntent.putParcelableArrayListExtra("SELECTED_MEMBERS", ArrayList(selectedMembers))
                setResult(Activity.RESULT_OK, resultIntent)
                Toast.makeText(
                    this@SelectMembers,
                    "Team duty started with ${selectedMembers.size} members",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }


    }
}
