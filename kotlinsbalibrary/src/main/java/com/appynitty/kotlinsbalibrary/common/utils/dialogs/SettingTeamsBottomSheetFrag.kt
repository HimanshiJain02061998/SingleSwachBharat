package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.Visibility
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.databinding.FragmentSettingBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SettingTeamsBottomSheetFrag : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private lateinit var binding: FragmentSettingBottomSheetBinding
    private var listener: SettingsTeamCallBack? = null
    private var isTeamsOn: Boolean = true

    fun setListener(listener: SettingsTeamCallBack) {
        this.listener = listener
    }

    fun setIsTeamsOn(on: Boolean) {
        this.isTeamsOn = on
    }

    companion object {
        const val TAG = "SettingTeamBottomSheetFrag"
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
        "Team On".also { binding.tvBifurcationType.text = it }
        binding.switchBifurcation.isChecked = isTeamsOn

        binding.switchBifurcation.setOnCheckedChangeListener { _, isChecked ->
            listener?.onSelectTeamsValueChanged(isChecked)
        }

        binding.switchVehicleScan.visibility = View.GONE
        binding.tvVehicleQR.visibility = View.GONE
        binding.versionCodeTvSettings.text = buildString {
            append("Version : ")
            append(CommonUtils.VERSION_CODE)
            append(".0")
        }
    }


    interface SettingsTeamCallBack {
        fun onSelectTeamsValueChanged(teamsOn: Boolean)
    }
}