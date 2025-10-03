package com.appynitty.kotlinsbalibrary.housescanify.ui.masterPlateActivity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.AirplaneModeChangeReceiver
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils.Companion.getGisServiceTimeStamp
import com.appynitty.kotlinsbalibrary.common.utils.GpsStatusListener
import com.appynitty.kotlinsbalibrary.common.utils.TurnOnGps
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityMasterPlateBinding
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner.EmpQrScannerActivity
import com.appynitty.kotlinsbalibrary.housescanify.utils.PhotoSubmitDialogFrag
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

private const val EXTRA_REFERENCE_ID = "referenceId"
private const val EXTRA_QR_IMAGE_FILE_PATH = "qrImageFilePath"
private const val EXTRA_PROPERTY_FILE_PATH = "propertyFilePath"
private const val EXTRA_UPDATED_LATITUDE = "updatedLatitude"
private const val EXTRA_UPDATED_LONGITUDE = "updatedLongitude"
private const val gcType = 12

@AndroidEntryPoint
class MasterPlateActivity : AppCompatActivity(), PhotoSubmitDialogFrag.PhotoSubmitEventListener {

    private var referenceId: String? = null
    private var qrImageFilePath: String? = null
    private var propertyFilePath: String? = null
    private var houseList = mutableListOf<String>()
    private lateinit var languageDataStore: LanguageDataStore
    private var languageId: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var updateLat: String? = null
    private var updateLong: String? = null
    private var userId: String? = null
    private var isGpsOn: Boolean = false
    private lateinit var receiver: AirplaneModeChangeReceiver
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var binding: ActivityMasterPlateBinding
    private var isInternetOn: Boolean = false
    private var isIsImgUpdate: Boolean = false
    private var isIsBunchUpdate: Boolean = false
    private lateinit var controller: LayoutAnimationController
    private lateinit var photoSubmitDialogFrag: PhotoSubmitDialogFrag
    private val viewModel: MasterPlateViewModel by viewModels()

    @Inject
    lateinit var userDataStore: UserDataStore

    private val qrScannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scannedQrCode = result.data?.getStringExtra("scanned_qr_code")?.uppercase(
                    Locale.ENGLISH
                )

                scannedQrCode?.let {
                    val pattern = "hpsba"
                    val regex = Regex(pattern, RegexOption.IGNORE_CASE)

                    if (regex.containsMatchIn(it)) {
                        viewModel.checkHouseIdExistsOrNot(it)
                    } else {
                        CustomToast.showWarningToast(this, resources.getString(R.string.qr_error))
                    }
                }
            }
        }

    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        if (activityResult.resultCode == RESULT_OK) {
            binding.gpsProgressLayout.visibility = View.VISIBLE

            Handler(Looper.myLooper()!!).postDelayed({
                if (isGpsOn) {
                    binding.gpsProgressLayout.visibility = View.GONE
                }

            }, 1000)

        } else if (activityResult.resultCode == RESULT_CANCELED) {
            // The user was asked to change settings, but chose not to
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()

        }
    }

    private fun addHouseToList(houseId: String) {

        if (!houseList.contains(houseId)) {
            houseList.add(0, houseId)
            houseAdapter.notifyItemInserted(0)
            binding.rvHouseList.scrollToPosition(0)
            isIsBunchUpdate = true

            binding.rvHouseList.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
            binding.rvHouseList.layoutAnimation = controller
        } else {
            CustomToast.showWarningToast(this, "House already added")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMasterPlateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
    }

    private fun initToolBar() {
        binding.toolbar.title = resources.getString(R.string.add_master_plate)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initVars() {
        receiver = AirplaneModeChangeReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        initToolBar()
        subscribeLiveData()
        subscribeChannelEvents()

        referenceId = intent.getStringExtra(EXTRA_REFERENCE_ID)
        qrImageFilePath = intent.getStringExtra(EXTRA_QR_IMAGE_FILE_PATH)
        propertyFilePath = intent.getStringExtra(EXTRA_PROPERTY_FILE_PATH)

        updateLat = intent.getStringExtra(EXTRA_UPDATED_LATITUDE)
        updateLong = intent.getStringExtra(EXTRA_UPDATED_LONGITUDE)

        controller =
            AnimationUtils.loadLayoutAnimation(
                this@MasterPlateActivity,
                R.anim.slide_layout_animation
            )

        languageDataStore = LanguageDataStore(this)
        languageId = languageDataStore.currentLanguage.languageId

        lifecycleScope.launchWhenStarted {
            userId = userDataStore.getUserEssentials.first().userId
        }

        val houseBunch = intent.getStringExtra("houseBunch")
        val idsArray = houseBunch?.split(",")
        if (idsArray != null) {
            for (id in idsArray) {
                if (id.isNotEmpty()) {
                    houseList.add(id.trim())
                }
            }
            binding.rvHouseList.layoutAnimation = controller
        }

        houseAdapter = HouseAdapter(houseList, object : HouseAdapter.DeleteClickListener {
            override fun onClickDelete(houseId: String, position: Int) {
                Log.e("MasterPlateActivity", "onClickDelete: $houseId}")


                AlertDialog.Builder(this@MasterPlateActivity)
                    .setTitle("Delete House ID")
                    .setMessage("Are you sure you want to delete $houseId ?")
                    .setPositiveButton("Yes") { _, _ ->
                        // User confirmed, delete the item
                        houseAdapter.removeItem(position, houseId)

                        if (houseList.isEmpty()) { // Handle empty list
                            binding.rvHouseList.visibility = View.GONE
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                    }
                    .setNegativeButton("No") { dialog, which ->
                        // User canceled, do nothing
                        dialog.dismiss()
                    }
                    .show()
            }

            override fun onItemDeleted(updatedList: MutableList<String>) {
                houseList = updatedList
                binding.rvHouseList.layoutAnimation = controller
                isIsBunchUpdate = true
                Log.e("MasterPlateActivity", "onItemDeleted: $updatedList")
                Log.e("MasterPlateActivity", "updatedHouseList: $houseList")
            }
        })

        photoSubmitDialogFrag = PhotoSubmitDialogFrag()
        photoSubmitDialogFrag.setListener(this)

        binding.apply {
            tvMasterId.text = referenceId

            if (houseList.isNotEmpty()) {
                rvHouseList.visibility = View.VISIBLE
                tvNoData.visibility = View.GONE
            }

            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnAddHouse.setOnClickListener {
                val intent = Intent(this@MasterPlateActivity, EmpQrScannerActivity::class.java)
                intent.putExtra("gcType", gcType) // Pass any necessary data
                qrScannerLauncher.launch(intent)
            }

            btnSubmit.setOnClickListener {
                submitMasterIdData()
            }

            btnChangePhoto.setOnClickListener {
                photoSubmitDialogFrag.languageId = languageId.toString()
                photoSubmitDialogFrag.gcType = gcType.toString()
                referenceId?.let { id -> photoSubmitDialogFrag.setReferenceId(id) }
                photoSubmitDialogFrag.isImgUpdate = true
                photoSubmitDialogFrag.show(
                    supportFragmentManager, PhotoSubmitDialogFrag.TAG
                )
            }

            rvHouseList.adapter = houseAdapter
        }

        Log.e(
            "MasterPlateActivity",
            "referenceId: $referenceId, qrImagePath: $qrImageFilePath, propertyImagePath: $propertyFilePath, updateLat: $updateLat, updateLong: $updateLong"
        )
    }

    private fun subscribeChannelEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.masterPlateActivityEventsFlow.collect { event ->
                when (event) {
                    is MasterPlateViewModel.MasterPlateActivityEvents.ShowWarningMessage -> {
                        CustomToast.showWarningToast(this@MasterPlateActivity, event.message)
                    }

                    is MasterPlateViewModel.MasterPlateActivityEvents.DeleteUploadedImages -> {
                        deleteUploadedImage(event.qrImagePath, event.propertyImagePath)
                    }

                    MasterPlateViewModel.MasterPlateActivityEvents.HideLoading -> {
                        binding.progressBar.visibility = View.GONE
                    }

                    is MasterPlateViewModel.MasterPlateActivityEvents.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@MasterPlateActivity, event.message)
                    }

                    MasterPlateViewModel.MasterPlateActivityEvents.ShowLoading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is MasterPlateViewModel.MasterPlateActivityEvents.ShowResponseErrorMessage -> {
                        showApiErrorMessage(event.msg, event.msgMr)
                    }

                    is MasterPlateViewModel.MasterPlateActivityEvents.ShowResponseSuccessMessage -> {
                        showApiSuccessMessage(event.msg, event.msgMr)
                    }

                    MasterPlateViewModel.MasterPlateActivityEvents.FinishActivity -> finishActivity()

                    is MasterPlateViewModel.MasterPlateActivityEvents.HouseIdExists -> {
                        addHouseToList(event.houseId)
                    }
                }
            }
        }
    }

    private fun subscribeLiveData() {

        receiver.airplaneModeLiveData.observe(this) {

            if (!it) {
                TurnOnGps.gpsStatusCheck(this, resolutionForResult)
            } else {
//                CustomToast.showWarningToast(
//                    this@EmpQrScannerActivity,
//                    resources.getString(R.string.turn_off_airplane_mode)
//                )
            }
        }

        GpsStatusListener(this).observe(this) {
            isGpsOn = it
            if (!it) {
                TurnOnGps.gpsStatusCheck(this, resolutionForResult)
            }
        }

        val snackBar = Snackbar.make(
            binding.clParent,
            resources.getString(R.string.no_internet_error),
            Snackbar.LENGTH_INDEFINITE
        )

        val internetConnectivity = ConnectivityStatus(this)

        internetConnectivity.observe(this) {
            isInternetOn = it

            if (it) {
                snackBar.dismiss()
            } else {
                snackBar.show()
            }
        }

        userDataStore.getUserLatLong.asLiveData().observe(this) {
            latitude = it.latitude
            longitude = it.longitude
        }
    }

    private fun submitMasterIdData() {
        val bunchString: String
        if (houseList.isNotEmpty()) {
            bunchString = houseList.joinToString(",")
        } else {
            CustomToast.showWarningToast(this@MasterPlateActivity, "Please add a house!")
            return
        }

        val mGeomPoint = StringBuilder("POINT (")
        mGeomPoint.append(updateLat ?: latitude)
        mGeomPoint.append(" ")
        mGeomPoint.append(updateLong ?: longitude)
        mGeomPoint.append(")")

        val geomPoint = mGeomPoint.toString()
        val date = getGisServiceTimeStamp()

        if (isInternetOn) {
            /** online stuff */

            val qrImageBitmap = BitmapFactory.decodeFile(qrImageFilePath)
            val qrBase64Image = qrImageBitmap?.let {
                CameraUtils.prepareBase64Images(
                    qrImageBitmap,
                    referenceId!!,
                    qrImageFilePath!!,
                    latitude = updateLat ?: latitude,
                    longitude = updateLong ?: longitude,
                    DateTimeUtils.getSimpleDateTime()
                )
            } ?: ""

            val propertyImageBitmap = BitmapFactory.decodeFile(propertyFilePath)

            val propertyBase64Image = propertyImageBitmap?.let {
                CameraUtils.prepareBase64Images(
                    propertyImageBitmap,
                    referenceId!!,
                    propertyFilePath!!,
                    latitude = updateLat ?: latitude,
                    longitude = updateLong ?: longitude,
                    DateTimeUtils.getSimpleDateTime()
                )
            } ?: ""

            val masterPlateData = EmpGarbageCollectionRequest(
                0,
                updateLat ?: latitude!!,
                updateLong ?: longitude!!,
                date,
                gcType.toString(),
                userId!!,
                referenceId!!,
                qrBase64Image,
                propertyBase64Image,
                geomPoint,
                date,
                "0",
                "",
                "",
                "",
                0,
                "",
                "",
                "",
                0,
                0,
                0,
                bunchString,
                isImageUpdated = isIsImgUpdate,
                isBunchUpdated = isIsBunchUpdate
            )

            viewModel.saveMasterPlateCollectionOnline(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                masterPlateData,
                qrImageFilePath,
                propertyFilePath
            )
        }
    }

    private fun showApiSuccessMessage(msg: String?, msgMr: String?) {
        if (languageId == "mr") {
            msgMr?.let {
                CustomToast.showSuccessToast(
                    this@MasterPlateActivity,
                    it
                )
            }
        } else {
            msg?.let {
                CustomToast.showSuccessToast(
                    this@MasterPlateActivity,
                    it
                )
            }
        }
    }

    private fun showApiErrorMessage(msg: String?, msgMr: String?) {
        if (languageId == "mr") {
            msgMr?.let {
                CustomToast.showErrorToast(
                    this@MasterPlateActivity,
                    msgMr
                )
            }
        } else {
            msg?.let {
                CustomToast.showErrorToast(
                    this@MasterPlateActivity,
                    msg
                )
            }
        }
    }

    private fun deleteUploadedImage(qrImageFilePath: String?, propertyImageFilePath: String?) {
        qrImageFilePath.let {
            if (it != null) {
                CameraUtils.deleteTheFile(it)
            }
        }
        propertyImageFilePath.let {
            if (it != null) {
                CameraUtils.deleteTheFile(it)
            }
        }
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }

    companion object {
        fun start(
            context: Context,
            refId: String,
            qrFilePath: String,
            propertyFilePath: String,
            updateLat: String? = null,
            updateLong: String? = null
        ) {
            val intent = Intent(context, MasterPlateActivity::class.java).apply {
                putExtra(EXTRA_REFERENCE_ID, refId)
                putExtra(EXTRA_QR_IMAGE_FILE_PATH, qrFilePath)
                putExtra(EXTRA_PROPERTY_FILE_PATH, propertyFilePath)
                putExtra(EXTRA_UPDATED_LATITUDE, updateLat)
                putExtra(EXTRA_UPDATED_LONGITUDE, updateLong)
            }
            context.startActivity(intent)
        }
    }

    override fun onPhotoSubmitBtnClicked(
        referenceId: String,
        qrImageFilePath: String,
        propertyImageFilePath: String,
        dialog: Dialog,
        propertyType: Int
    ) {
        isIsImgUpdate = true
        this.qrImageFilePath = qrImageFilePath
        this.propertyFilePath = propertyImageFilePath
        dialog.dismiss()
    }

    override fun onDialogDismissed() {
        Log.e("TAG", "onDialogDismissed: ")
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if (ev.pointerCount > 1) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}