package com.appynitty.kotlinsbalibrary.common.ui.select_ulb_module

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.model.response.District
import com.appynitty.kotlinsbalibrary.common.model.response.ULB
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
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
    private var districtList: List<District> = emptyList()
    private var ulbList: List<ULB> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUlbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDataStore = UserDataStore(this)

        // Fetch District list
        districtViewModel.fetchDistrictList()

        // --- Observe District List ---
        lifecycleScope.launch {
            districtViewModel.districtList.collectLatest { districts ->
                if (districts.isNotEmpty()) {
                    val districtNames = mutableListOf("Select District")
                    districtNames.addAll(districts.map { it.DistrictName })
                    districtList = districts

                    val districtAdapter = ArrayAdapter(
                        this@SelectUlb,
                        android.R.layout.simple_dropdown_item_1line,
                        districtNames
                    )
                    binding.autoDistrict.setAdapter(districtAdapter)

                    binding.autoDistrict.setOnItemClickListener { parent, _, position, _ ->
                        if (position == 0) {
                            // "Select District" clicked → reset ULB list
                            binding.autoUlb.setText("")
                            binding.autoUlb.setAdapter(null)
                            selectedAppId = null
                        } else {
                            val selectedName = parent.getItemAtPosition(position).toString()
                            val selectedDistrict = districtList.find { it.DistrictName == selectedName }

                            selectedDistrict?.let {
                                lifecycleScope.launch { userDataStore.saveDisId(it.Disid) }

                                // Clear previous ULB selection
                                binding.autoUlb.setText("")
                                selectedAppId = null

                                // Fetch ULB list for this district
                                districtViewModel.fetchULBList(it.Disid)
                            }
                            binding.autoDistrict.clearFocus()
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                            imm.hideSoftInputFromWindow(binding.autoDistrict.windowToken, 0)
                        }
                    }
                }
            }
        }


        // --- Observe ULB List ---
        lifecycleScope.launch {
            districtViewModel.ulbList.collectLatest { ulbs ->
                if (ulbs.isNotEmpty()) {
                    ulbList = ulbs
                    val ulbNames = ulbs.map { it.ulbName }

                    val ulbAdapter = ArrayAdapter(
                        this@SelectUlb,
                        android.R.layout.simple_dropdown_item_1line,
                        ulbNames
                    )
                    binding.autoUlb.setAdapter(ulbAdapter)

                    binding.autoUlb.setOnItemClickListener { parent, _, position, _ ->
                        val selectedName = parent.getItemAtPosition(position).toString()
                        val selectedULB = ulbList.find { it.ulbName == selectedName }
                        selectedAppId = selectedULB?.appId?.toString()

                        // --- Remove focus and hide keyboard ---
                        binding.autoUlb.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                        imm.hideSoftInputFromWindow(binding.autoUlb.windowToken, 0)
                    }

                } else {
                    // Clear when no ULB found
                    binding.autoUlb.setAdapter(null)
                    binding.autoUlb.setText("")
                    selectedAppId = null
                }
            }
        }


        // --- Button Click ---
        binding.btnAhead.setOnClickListener {
            val districtSelected = binding.autoDistrict.text.isNotEmpty()
            val ulbSelected = !selectedAppId.isNullOrEmpty()

            when {
                !districtSelected -> CustomToast.showWarningToast(this, "Please select a District first")
                !ulbSelected -> CustomToast.showWarningToast(this, "Please select a ULB before continuing")
                else -> {
                    lifecycleScope.launch {
                        userDataStore.saveAppId(selectedAppId!!)
                        MyApplication.APP_ID = selectedAppId!!

                        CustomToast.showSuccessToast(this@SelectUlb, "ULB selected successfully")

                        startActivity(Intent(this@SelectUlb, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}
