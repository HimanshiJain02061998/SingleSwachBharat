package com.appynitty.kotlinsbalibrary.housescanify.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraActivity
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.FragmentPhotoSubmitDialogBinding
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType
import com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner.PropertyTypeRecyclerAdapter
import com.appynitty.kotlinsbalibrary.housescanify.ui.masterPlateActivity.MasterPlateActivity
import com.bumptech.glide.Glide


class PhotoSubmitDialogFrag :
    DialogFragment(), PropertyTypeRecyclerAdapter.ItemSelectionListener {

    private var referenceId: String = ""
    private var qrImageFilePath: String = ""
    private var propertyFilePath: String = ""
    private var updateLatitude: String? = null
    private var updateLongitude: String? = null
    private lateinit var adapter: PropertyTypeRecyclerAdapter
    private var propertyTypeList = mutableListOf<PropertyType>()
    private var propertyTypeConst: Int = 0
    var languageId = "mr"
    var gcType = "1"
    var isImgUpdate = false
    private lateinit var openCameraActivity: ActivityResultLauncher<Intent>

    fun setReferenceId(referenceId: String) {
        this.referenceId = referenceId
    }

    fun setPropertyTypeList(propertyTypeList: List<PropertyType>) {
        this.propertyTypeList = propertyTypeList.toMutableList()
        Log.i(TAG, "setPropertyTypeList: ${propertyTypeList.size}")

    }

    companion object {
        const val TAG = "PhotoSubmitDialogFrag"
    }

    private var listener: PhotoSubmitEventListener? = null
    fun setListener(listener: PhotoSubmitEventListener) {
        this.listener = listener
    }

    private lateinit var binding: FragmentPhotoSubmitDialogBinding

    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openCameraActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val filePath = result.data?.getStringExtra(CameraUtils.IMAGE_PATH).toString()
                    if (filePath.isNotBlank()) {
                        val requestCode: Int? =
                            result.data?.getIntExtra(CameraActivity.REQUEST_ID, 0)

                        if (requestCode == 1) {
                            qrImageFilePath = filePath
                            Glide.with(requireContext()).load(filePath).centerCrop()
                                .into(binding.qrPhotoIv)
                        } else {
                            propertyFilePath = filePath
                            Glide.with(requireContext()).load(filePath).centerCrop()
                                .into(binding.propertyPhotoIv)
                        }

                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPhotoSubmitDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVars()
        registerClickEvents()
    }

    private fun setUpPropertyTypeRecyclerView() {
        Log.i(TAG, "setUpPropertyTypeRecyclerView: ${propertyTypeList.size}")
        adapter = PropertyTypeRecyclerAdapter(propertyTypeList)
        adapter.setListener(this)
        adapter.setLanguageId(languageId)
        binding.propertyTypeRecyclerView.setHasFixedSize(true)
        binding.propertyTypeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.propertyTypeRecyclerView.adapter = adapter
    }


    private fun initVars() {
        updateLatitude = arguments?.getString("updateLat")
        updateLongitude = arguments?.getString("updateLong")
        //  val bitmap = BitmapFactory.decodeFile(filePath)
        binding.txtHouseId.text = referenceId
        //binding.imgQRimg.setImageBitmap(bitmap)


        binding.card2.visibility = View.VISIBLE
        binding.afterTv.visibility = View.VISIBLE
//        when (gcType) {
//            "1" -> {
//                binding.card2.visibility = View.VISIBLE
//                binding.afterTv.visibility = View.VISIBLE
//            }
//
//            else -> {
//                binding.card2.visibility = View.GONE
//                binding.afterTv.visibility = View.GONE
//                binding.nextButton.text = resources.getString(R.string.submit_txt)
//            }
//        }

        when (gcType) {
            "1" -> {
                //house

                binding.afterTv.text = context?.resources?.getString(R.string.take_property_photo)
            }

            "3" -> {
                //dump
                binding.afterTv.text = context?.resources?.getString(R.string.take_dump_point_photo)
                binding.nextButton.text = resources.getString(R.string.submit_txt)
            }

            "4" -> {
                //liquid

                binding.afterTv.text =
                    context?.resources?.getString(R.string.take_liquid_point_photo)
                binding.nextButton.text = resources.getString(R.string.submit_txt)
            }

            "5" -> {
                //street
                binding.afterTv.text =
                    context?.resources?.getString(R.string.take_street_point_photo)
                binding.nextButton.text = resources.getString(R.string.submit_txt)
            }
        }
    }

    private fun registerClickEvents() {

        binding.qrPhotoIv.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(requireContext(), 1))
        }
        binding.propertyPhotoIv.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(requireContext(), 2))
        }
        binding.nextButton.setOnClickListener {

            if (binding.nextButton.text == resources.getString(R.string.submit_txt)) {

                if (propertyTypeList.isNotEmpty()) {
                    if (propertyTypeConst == 0) {
                        CustomToast.showWarningToast(
                            requireContext(),
                            resources.getString(R.string.select_propety_type)
                        )
                    } else {

                        dialog?.let { it1 ->
                            listener?.onPhotoSubmitBtnClicked(
                                referenceId,
                                qrImageFilePath,
                                propertyFilePath,
                                it1,
                                propertyTypeConst
                            )
                        }
                    }
                } else {
                    if (qrImageFilePath.isNotBlank() && propertyFilePath.isNotBlank()) {
                        dialog?.let { it1 ->
                            listener?.onPhotoSubmitBtnClicked(
                                referenceId,
                                qrImageFilePath,
                                propertyFilePath,
                                it1,
                                propertyTypeConst
                            )
                        }
                    } else {
                        CustomToast.showWarningToast(
                            requireContext(),
                            resources.getString(R.string.please_capture_both_photos)
                        )
                    }

                }
            } else {
                //  if (gcType == "1") {
                if (qrImageFilePath.isNotBlank() && propertyFilePath.isNotBlank()) {
                    if (propertyTypeList.isNotEmpty()) {
                        binding.propertyTypeRecyclerView.visibility = View.VISIBLE
                        setUpPropertyTypeRecyclerView()
                    }

                    if (gcType != "12") {
                        binding.nextButton.text = resources.getString(R.string.submit_txt)
                    } else {
                        if (isImgUpdate) {
                            dialog?.let { it1 ->
                                listener?.onPhotoSubmitBtnClicked(
                                    referenceId,
                                    qrImageFilePath,
                                    propertyFilePath,
                                    it1,
                                    propertyTypeConst
                                )
                            }
                        } else {
                            MasterPlateActivity.start(
                                requireActivity(),
                                referenceId,
                                qrImageFilePath,
                                propertyFilePath,
                                updateLat = updateLatitude,
                                updateLong = updateLongitude
                            )
                            requireActivity().finish()
                        }

                    }

                } else {
                    CustomToast.showWarningToast(
                        requireContext(),
                        resources.getString(R.string.please_capture_both_photos)
                    )
                }
                //  }
//                else {
//                    if (qrImageFilePath.isNotBlank()) {
//                        if (propertyTypeList.isNotEmpty()) {
//                            binding.propertyTypeRecyclerView.visibility = View.VISIBLE
//                            setUpPropertyTypeRecyclerView()
//                        }
//                    } else {
//                        CustomToast.showWarningToast(
//                            requireContext(),
//                            resources.getString(R.string.please_capture_qr_photo)
//                        )
//                    }
//                }

            }

        }

    }

    interface PhotoSubmitEventListener {

        fun onPhotoSubmitBtnClicked(
            referenceId: String,
            qrImageFilePath: String,
            propertyImageFilePath: String,
            dialog: Dialog,
            propertyType: Int
        )

        fun onDialogDismissed()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        qrImageFilePath = ""
        propertyFilePath = ""
        listener?.onDialogDismissed()
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
            dialog!!.window
                ?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(propertyType: PropertyType) {
        propertyTypeConst = propertyType.Property_Id!!
    }

}