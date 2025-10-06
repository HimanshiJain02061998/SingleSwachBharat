package com.appynitty.kotlinsbalibrary.common.ui.select_ulb_module

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUlbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDataStore = UserDataStore(this)

        // Fetch districts
        districtViewModel.fetchDistrictList()

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

                                    // Save selected disId in DataStore
                                    lifecycleScope.launch {
                                        userDataStore.saveDisId(selectedDistrict.Disid)
                                    }

                                    districtViewModel.fetchULBList(selectedDistrict.Disid)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }
        }

        // Observe ULB list
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
                                    // Use selectedULB if needed
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }
        }
    }
}

