package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.databinding.FragmentSettingBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SettingBottomSheetFrag : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private lateinit var binding: FragmentSettingBottomSheetBinding
    private var listener: SettingsCallBack? = null
    private var isBifurcationOn: Boolean = true
    private var isVehicleScanOn: Boolean = false
    private var empType: String? = null
    fun setEmpType(empType : String?){
        this.empType = empType
    }

    fun setListener(listener: SettingsCallBack) {
        this.listener = listener
    }

    fun setIsBifurcationOn(isBifurcationOn: Boolean) {
        this.isBifurcationOn = isBifurcationOn
    }

    fun setIsVehicleScanOn(isVehicleScanOn: Boolean) {
        this.isVehicleScanOn = isVehicleScanOn
    }

    companion object {
        const val TAG = "SettingBottomSheetFrag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        if (savedInstanceState != null) {
            dismiss()
        }
        binding = FragmentSettingBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.switchVehicleScan.visibility = View.VISIBLE
        binding.tvVehicleQR.visibility = View.VISIBLE
        binding.switchVehicleScan.isChecked = isVehicleScanOn

        binding.switchVehicleScan.setOnCheckedChangeListener { _, isChecked ->
            listener?.onVehicleScanValueChanged(isChecked)
        }


        binding.switchBifurcation.isChecked = isBifurcationOn

        binding.switchBifurcation.setOnCheckedChangeListener { _, isChecked ->
            listener?.onBifurcationValueChanged(isChecked)
        }

        binding.versionCodeTvSettings.text = buildString {
            append("Version : ")
            append(CommonUtils.VERSION_CODE)
            append(".0")
        }

        hideShowViews()
    }

    private fun hideShowViews() {
        when (empType) {
            "D" -> {
                binding.switchBifurcation.visibility = View.GONE
                binding.tvBifurcationType.visibility = View.GONE
            }
            else -> {
                binding.tvVehicleQR.visibility = View.GONE
                binding.switchVehicleScan.visibility = View.GONE
            }
        }
    }


    interface SettingsCallBack {
        fun onBifurcationValueChanged(isBifurcationOn: Boolean)
        fun onVehicleScanValueChanged(isVehicleScanOn: Boolean)

    }
}