package com.appynitty.kotlinsbalibrary.common.utils.dialogs

import android.content.DialogInterface
import android.graphics.Path.Direction
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.adapter.CustomDropDownAdapter
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.adapter.PopUpDialogAdapter
import com.appynitty.kotlinsbalibrary.databinding.FragmentPopUpDialogBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleNumberResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleTypeResponse


class PopUpDialogFragment : DialogFragment(), PopUpDialogAdapter.PopUpDialogItemClickListener {

    private lateinit var binding: FragmentPopUpDialogBinding
    private var vehicleTypeList: ArrayList<VehicleTypeResponse>? = null
    private var popUpDialogFragmentClickListeners: PopUpDialogFragmentClickListeners? = null
    private var vehicleNumberList: List<VehicleNumberResponse>? = null
    private var dumpYardIdsList: List<String>? = null
    private var vehicleId: String? = null
    private var vehicleTypeName: String? = null
    private lateinit var popUpDialogAdapter: PopUpDialogAdapter
    private var isSubmitBtnClicked = false

    fun setVehicleTypeList(vehicleTypeList: ArrayList<VehicleTypeResponse>) {
        this.vehicleTypeList = vehicleTypeList

    }

    fun setListener(popUpDialogFragmentClickListeners: PopUpDialogFragmentClickListeners) {
        this.popUpDialogFragmentClickListeners = popUpDialogFragmentClickListeners
    }

    fun showProgressBar() {
        if (context != null)
            binding.dialogProgressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        if (context != null)
            binding.dialogProgressBar.visibility = View.GONE
    }

    fun setVehicleNumberList(vehicleNumberList: List<VehicleNumberResponse>) {

        this.vehicleNumberList = vehicleNumberList

        val mList = ArrayList<String>()

        for (i in vehicleNumberList.indices) {
            mList.add(vehicleNumberList[i].VehicleNo)
        }
        if (context != null) {
            val arrayAdapter = CustomDropDownAdapter(requireContext(), R.layout.drop_down_emp_type, mList)
            binding.etVehicleNumber.setAdapter(arrayAdapter)
        }

    }

    fun setDumpYardIds(dumpYardIdsList: List<String>) {

        this.dumpYardIdsList = dumpYardIdsList

    }

    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPopUpDialogBinding.inflate(inflater)

        if (savedInstanceState != null) {
            dismiss()
        }
        return binding.root
    }


    companion object {
        const val TAG = "PopUpDialogFragment"

    }

    override fun onResume() {
        super.onResume()
        Log.d("DialogVehicle", "onResume: dialog pop up")
    }

    override fun onStart() {
        super.onStart()

        // safety check
        if (dialog == null) {
            return
        } else {
            dialog!!.window?.setWindowAnimations(
                R.style.dialog_animation_fade
            )
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (dumpYardIdsList != null) {
            if (context != null) {

                binding.dialogTitle.text = resources.getString(R.string.select_dump_yard)

                binding.dialogRecyclerView.visibility = View.GONE
                binding.constraintDialog.visibility = View.VISIBLE
                binding.etVehicleNumber.hint = resources.getString(R.string.select_dump_yard_id)
                val arrayAdapter = CustomDropDownAdapter(
                    requireContext(), R.layout.drop_down_emp_type,
                    dumpYardIdsList!!
                )
                binding.etVehicleNumber.setAdapter(arrayAdapter)
            }
        } else {
            binding.dialogTitle.text = resources.getString(R.string.dialog_title_txt_vehicle)
            setUpRecyclerView()

        }
        registerClickEvents()
    }

    private fun registerClickEvents() {
        binding.btnSubmit.setOnClickListener {

            val vehicleNumber = binding.etVehicleNumber.text.toString()
            if (vehicleNumber.isEmpty()) {
                context?.let { it1 ->

                    if (dumpYardIdsList != null){
                        CustomToast.showWarningToast(
                            it1, resources.getString(R.string.please_select_dump_yard_id)
                        )
                    }else{
                        CustomToast.showWarningToast(
                            it1, resources.getString(R.string.noVehicleNo)
                        )
                    }
                }
            } else {
                if (dumpYardIdsList != null) {
                    isSubmitBtnClicked = true
                    var isValidIdSelected = false
                    dumpYardIdsList?.forEach {
                        if (it == vehicleNumber) {
                            isValidIdSelected = true
                        }
                    }
                    if (isValidIdSelected) {
                        popUpDialogFragmentClickListeners?.onDumpYardIdSelected(
                            vehicleNumber
                        )
                        dismiss()
                    } else {
                        CustomToast.showWarningToast(
                            requireContext(), resources.getString(R.string.noVehicleNo)
                        )
                    }

                } else {
                    vehicleId?.let { it1 ->
                        isSubmitBtnClicked = true
                        var isValidIdSelected = false
                        vehicleNumberList?.forEach {
                            if (it.VehicleNo == vehicleNumber) {
                                isValidIdSelected = true
                            }
                        }
                        if (isValidIdSelected){
                            vehicleTypeName?.let { it2 ->
                                popUpDialogFragmentClickListeners?.onVehicleDialogSubmitBtnClicked(
                                    it1, it2, vehicleNumber
                                )
                            }
                            dismiss()
                        } else {
                            CustomToast.showWarningToast(
                                requireContext(), resources.getString(R.string.noVehicleNo)
                            )
                        }

                    }
                }

            }
        }
    }


    private fun setUpRecyclerView() {
        binding.dialogRecyclerView.setHasFixedSize(true)
        binding.dialogRecyclerView.layoutManager = LinearLayoutManager(context)
        if (vehicleTypeList != null) {
            popUpDialogAdapter = vehicleTypeList?.let { PopUpDialogAdapter(vehicleTypeList!!) }!!
            binding.dialogRecyclerView.adapter = popUpDialogAdapter
            popUpDialogAdapter.setListener(this)
        }
    }


    override fun onDismiss(dialog: DialogInterface) {

        binding.etVehicleNumber.setText("")

        if (!isSubmitBtnClicked) popUpDialogFragmentClickListeners?.onVehicleDialogDismissed(true)

        isSubmitBtnClicked = false

        super.onDismiss(dialog)
    }

    override fun onDialogItemClicked(vehicleId: String, vehicleTypeName: String) {

        this.vehicleId = vehicleId
        this.vehicleTypeName = vehicleTypeName
        popUpDialogFragmentClickListeners?.onVehicleDialogItemClicked(vehicleId, vehicleTypeName)
        binding.dialogRecyclerView.visibility = View.GONE
        binding.constraintDialog.visibility = View.VISIBLE

    }

    interface PopUpDialogFragmentClickListeners {
        fun onVehicleDialogSubmitBtnClicked(
            vehicleId: String, vehicleTypeName: String, vehicleNumber: String
        )

        fun onVehicleDialogItemClicked(vehicleId: String, vehicleTypeName: String)
        fun onVehicleDialogDismissed(isDismissWithoutSubmit: Boolean)
        fun onDumpYardIdSelected(dumpYardId: String)
    }

}