package com.appynitty.kotlinsbalibrary.common.ui.select_ulb_module

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.viewmodel.DistrictViewModel
import com.appynitty.kotlinsbalibrary.databinding.ActivitySelectUlbBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectUlb : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUlbBinding
    private val districtViewModel: DistrictViewModel by viewModels()
    private lateinit var userDataStore: UserDataStore

    private var selectedAppId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUlbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDataStore = UserDataStore(this)


        districtViewModel.fetchDistrictList()

        // Observe District List
        lifecycleScope.launch {
            districtViewModel.districtList.collectLatest { districts ->
                if (districts.isNotEmpty()) {
                    val districtNames = mutableListOf("Select District")
                    districtNames.addAll(districts.map { it.DistrictName })

                    val adapter = ArrayAdapter(
                        this@SelectUlb,
                        android.R.layout.simple_spinner_item,
                        districtNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerDistrict.adapter = adapter

                    binding.spinnerDistrict.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: android.view.View?,
                                position: Int,
                                id: Long
                            ) {
                                if (position > 0) {
                                    val selectedDistrict = districts[position - 1]
                                    lifecycleScope.launch {
                                        userDataStore.saveDisId(selectedDistrict.Disid)
                                    }
                                    districtViewModel.fetchULBList(selectedDistrict.Disid)
                                }
                                else {
                                    // Reset ULB spinner if "Select District" is chosen again
                                    resetUlbSpinner()
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }
        }



        // Observe ULB List
        lifecycleScope.launch {
            districtViewModel.ulbList.collectLatest { ulbs ->
                if (ulbs.isNotEmpty()) {
                    val ulbNames = mutableListOf("Select ULB")
                    ulbNames.addAll(ulbs.map { it.ulbName })

                    val adapter = ArrayAdapter(
                        this@SelectUlb,
                        android.R.layout.simple_spinner_item,
                        ulbNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerUlb.adapter = adapter

                    binding.spinnerUlb.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: android.view.View?,
                                position: Int,
                                id: Long
                            ) {
                                if (position > 0) {
                                    val selectedULB = ulbs[position - 1]
                                    selectedAppId =
                                        selectedULB.appId.toString()  // Save AppId temporarily
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }
        }

        binding.btnAhead.setOnClickListener {
            val districtSelected = binding.spinnerDistrict.selectedItemPosition > 0
            val ulbSelected = !selectedAppId.isNullOrEmpty()

            when {
                !districtSelected -> CustomToast.showWarningToast(this, "Please select a District first")
                !ulbSelected -> CustomToast.showWarningToast(this, "Please select a ULB before continuing")
                else -> {
                    lifecycleScope.launch {
                        userDataStore.saveAppId(selectedAppId!!)

                        MyApplication.APP_ID = selectedAppId!!


                        CustomToast.showSuccessToast(this@SelectUlb, "ULB selected successfully")

                        val intent = Intent(this@SelectUlb, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


    }
    private fun resetUlbSpinner() {
        val emptyList = listOf("Select ULB")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            emptyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUlb.adapter = adapter

        selectedAppId = null
    }

}
