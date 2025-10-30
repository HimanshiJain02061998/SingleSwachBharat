package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import com.appynitty.kotlinsbalibrary.databinding.ActivitySelectMembersBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectMembers : AppCompatActivity() {

    private lateinit var binding: ActivitySelectMembersBinding
    private val employeeViewModel: EmployeeViewModel by viewModels()
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
        userId = intent.getStringExtra("USER_ID")
        latitude = intent.getDoubleExtra("latitude", 0.0).toString()
        longitude = intent.getDoubleExtra("longitude", 0.0).toString()


        binding.recyclerMembers.layoutManager = LinearLayoutManager(this)
        adapter = MemberAdapter(mutableListOf())
        binding.recyclerMembers.adapter = adapter

        binding.tvSelectedCount.text = buildString {
            append(getString(R.string.selected_0))
            append(0)
        }

        employeeViewModel.employeeList.observe(this) { employees ->
            adapter.updateList(employees)
        }
        adapter.onSelectionChanged = { count ->
            binding.tvSelectedCount.text = buildString {
                append(getString(R.string.selected_0))
                append(count)
            }
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
                CustomToast.showWarningToast(this, getString(R.string.please_select_at_least_one_member))
                return@setOnClickListener
            }

            if (userId.isNullOrEmpty()) {
                CustomToast.showWarningToast(this, getString(R.string.user_id_not_found))
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val resultIntent = Intent()
                resultIntent.putParcelableArrayListExtra("SELECTED_MEMBERS", ArrayList(selectedMembers))
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }


    }
}
