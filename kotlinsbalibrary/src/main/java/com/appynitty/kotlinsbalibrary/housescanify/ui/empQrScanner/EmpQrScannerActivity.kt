package com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.AirplaneModeChangeReceiver
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils.Companion.getGisServiceTimeStamp
import com.appynitty.kotlinsbalibrary.common.utils.GpsStatusListener
import com.appynitty.kotlinsbalibrary.common.utils.TurnOnGps
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.permission.CameraPermission
import com.appynitty.kotlinsbalibrary.databinding.ActivityEmpQrScannerBinding
import com.appynitty.kotlinsbalibrary.housescanify.model.EmpHouseOnMap
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.response.PropertyType
import com.appynitty.kotlinsbalibrary.housescanify.ui.mapsActivity.MapsActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.masterPlateActivity.MasterPlateActivity
import com.appynitty.kotlinsbalibrary.housescanify.utils.PhotoSubmitDialogFrag
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class EmpQrScannerActivity : AppCompatActivity(), PhotoSubmitDialogFrag.PhotoSubmitEventListener {

    private lateinit var binding: ActivityEmpQrScannerBinding

    private val viewModel: EmpQrViewModel by viewModels()
    private lateinit var receiver: AirplaneModeChangeReceiver

    @Inject
    lateinit var userDataStore: UserDataStore

    private var empType: String? = null
    private var userId: String? = null
    private var languageId: String? = null
    private var userTypeId: String? = null
    private lateinit var beepManager: BeepManager
    private lateinit var scannerView: DecoratedBarcodeView
    private var isFlashLightOn = false
    private var extractedQRCode: String? = null
    private var isGpsOn: Boolean = false
    private var latitude: String? = null
    private var longitude: String? = null
    private var updatedLatitude: String? = null
    private var updatedLongitude: String? = null
    private var referenceId: String? = null
    private var isInternetOn: Boolean = false
    private var isPhotoDialogVisible: Boolean = false
    private var shouldResumeScanner: Boolean = false
    private lateinit var photoSubmitDialogFrag: PhotoSubmitDialogFrag
    private var propertyTypeList = mutableListOf<PropertyType>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmpQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scannerView = binding.scannerView.qrScanner1

        //on configuration changes
        if (savedInstanceState != null) {
            isPhotoDialogVisible = savedInstanceState.getBoolean("IS_PHOTO_DIALOG_VISIBLE")
            Log.i("IS_PHOTO_DIALOG_VISIBLE", "onCreate: $isPhotoDialogVisible")
            if (isPhotoDialogVisible) {
                finish()
            }
        }

        initVars()

        receiver = AirplaneModeChangeReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        initCameraPermission()
        setUpFlashFab()
        setUpBarcodeView()
        subscribeLiveData()
        subscribeChannelEvents()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    private fun subscribeChannelEvents() {

        lifecycleScope.launchWhenStarted {
            viewModel.empQrEventsFlow.collect { event ->

                when (event) {
                    is EmpQrViewModel.EmpQrEvent.ShowWarningMessage -> {
                        showWarningToast(event.resourceId)
                    }

                    EmpQrViewModel.EmpQrEvent.StartMapActivityForResult -> {
                        startMapActivityForResults()
                    }

                    EmpQrViewModel.EmpQrEvent.ResumeQrScanner -> {
                        resumeScanner()
                    }

                    is EmpQrViewModel.EmpQrEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@EmpQrScannerActivity, event.msg)
                    }

                    EmpQrViewModel.EmpQrEvent.ShowLoading -> {
                        showLoading()
                    }

                    EmpQrViewModel.EmpQrEvent.HideLoading -> {
                        hideLoading()
                    }

                    EmpQrViewModel.EmpQrEvent.FinishActivity -> finishActivity()
                    is EmpQrViewModel.EmpQrEvent.ShowResponseErrorMessage -> showApiErrorMessage(
                        event.msg, event.msgMr
                    )

                    is EmpQrViewModel.EmpQrEvent.ShowResponseSuccessMessage -> showApiSuccessMessage(
                        event.msg, event.msgMr
                    )

                    is EmpQrViewModel.EmpQrEvent.ShowSuccessToast -> {
                        showSuccessToast(event.resourceId)
                    }

                    EmpQrViewModel.EmpQrEvent.LoadHouseOnMapHistory -> getHouseOnMapList()

                    is EmpQrViewModel.EmpQrEvent.DeleteUploadedImage -> deleteUploadedImage(
                        event.qrImagePath, event.propertyImagePath
                    )

                    is EmpQrViewModel.EmpQrEvent.NavigateToMasterPlateActivity -> {
                        val intent =
                            Intent(this@EmpQrScannerActivity, MasterPlateActivity::class.java)
                        intent.putExtra("referenceId", event.referenceId)
                        intent.putExtra("houseBunch", event.housesList)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }

    private fun showSuccessToast(resourceId: Int) {
        CustomToast.showSuccessToast(this, resources.getString(resourceId))
    }

    private fun showApiSuccessMessage(msg: String?, msgMr: String?) {
        if (languageId == "mr") {
            msgMr?.let {
                CustomToast.showSuccessToast(this, msgMr)
            }
        } else {
            msg?.let {
                CustomToast.showSuccessToast(this, msg)
            }
        }
    }

    private fun showApiErrorMessage(msg: String?, msgMr: String?) {
        if (languageId == "mr") {
            msgMr?.let {
                CustomToast.showErrorToast(this, msgMr)
            }
        } else {
            msg?.let {
                CustomToast.showErrorToast(this, msg)
            }
        }
    }

    private fun deleteUploadedImage(qrImageFilePath: String, propertyImageFilePath: String) {
        qrImageFilePath.let { CameraUtils.deleteTheFile(it) }
        propertyImageFilePath.let { CameraUtils.deleteTheFile(it) }
    }

    private fun hideLoading() {
        binding.scannerView.progressBar.visibility = View.GONE
        binding.scannerView.transparentWhiteBg.visibility = View.GONE
        binding.flashToggle.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.scannerView.progressBar.visibility = View.VISIBLE
        binding.scannerView.transparentWhiteBg.visibility = View.VISIBLE
        binding.flashToggle.visibility = View.INVISIBLE
    }

    private fun startMapActivityForResults() {
        val mapsIntent = Intent(this, MapsActivity::class.java)
        mapsIntent.putExtra("latitude", latitude?.toDouble())
        mapsIntent.putExtra("longitude", longitude?.toDouble())
        mapsIntent.putParcelableArrayListExtra("houseOnMapList", viewModel.houseOnMapList)
        mapActivityLauncher.launch(mapsIntent)
    }

    private fun showWarningToast(resourceId: Int) {
        CustomToast.showWarningToast(this, resources.getString(resourceId))
    }

    private fun getHouseOnMapList() {

        if (isInternetOn) {
            viewModel.getHouseOnMapHistory(
                CommonUtils.APP_ID, userId!!, DateTimeUtils.getYyyyMMddDate()
            )
        } else {
            viewModel.getHouseOnMapHistoryFromRoom()
        }

    }

    fun handleQrResult(extractedCode: String) {
        pauseScanner()

        referenceId = extractedCode.trim()

        if (intent.hasExtra("gcType")) {
            val gcType = intent.getIntExtra("gcType", 0)
            if (gcType == 12) {
                val resultIntent = Intent()
                resultIntent.putExtra("scanned_qr_code", referenceId)
                setResult(Activity.RESULT_OK, resultIntent)
                finishActivity()
            }
        } else {
            if (!isInternetOn && referenceId!!.matches("^[Mm]-\\d{1,6}\$".toRegex())) {
                CustomToast.showWarningToast(this, "Offline scanning is not allowed")
                finishActivity()
            } else {
                viewModel.validateScannedQrCode(referenceId!!)
            }

        }

    }

    private fun submitEmpGc(
        referenceId: String, qrImageFilePath: String,
        propertyImageFilePath: String, propertyType: Int
    ) {

        val newLat: String
        val newLong: String

        if (updatedLatitude != null && updatedLatitude!!.isNotEmpty()) {

            newLat = updatedLatitude!!
            newLong = updatedLongitude!!
        } else {

            newLat = latitude!!
            newLong = longitude!!
        }

        val mGeomPoint = StringBuilder("POINT (")
        mGeomPoint.append(newLong)
        mGeomPoint.append(" ")
        mGeomPoint.append(newLat)
        mGeomPoint.append(")")

        val geomPoint = mGeomPoint.toString()
        val date = getGisServiceTimeStamp()

        val empHouseOnMap = EmpHouseOnMap(referenceId, newLat, newLong, viewModel.gcType.toInt())
        if (isInternetOn) {
            /** online stuff */

            val qrImageBitmap = BitmapFactory.decodeFile(qrImageFilePath)
            val qrBase64Image = CameraUtils.prepareBase64Images(
                qrImageBitmap,
                referenceId,
                qrImageFilePath,
                newLat,
                newLong,
                DateTimeUtils.getSimpleDateTime()
            )

            val propertyBase64Image: String?
            //   if (viewModel.gcType == "1") {
            val propertyImageBitmap = BitmapFactory.decodeFile(propertyImageFilePath)

            propertyBase64Image = CameraUtils.prepareBase64Images(
                propertyImageBitmap,
                referenceId,
                propertyImageFilePath,
                newLat,
                newLong,
                DateTimeUtils.getSimpleDateTime()
            )
            //  }


            if (qrBase64Image.isNotEmpty()) {
                val empGarbageCollectionRequest = EmpGarbageCollectionRequest(
                    0,
                    newLat,
                    newLong,
                    date,
                    viewModel.gcType,
                    userId!!,
                    referenceId,
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
                    propertyType
                )

                viewModel.saveGarbageCollectionOnlineDataToApi(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    empGarbageCollectionRequest,
                    empHouseOnMap,
                    qrImageFilePath,
                    propertyImageFilePath
                )
            } else {
                CustomToast.showWarningToast(this, resources.getString(R.string.please_scan_again))
            }

        } else {
            /** offline stuff */

            if (qrImageFilePath.isNotEmpty() && propertyImageFilePath.isNotEmpty()) {

                val empGarbageCollectionRequest = EmpGarbageCollectionRequest(
                    0,
                    newLat,
                    newLong,
                    date,
                    viewModel.gcType,
                    userId!!,
                    referenceId,
                    qrImageFilePath,
                    propertyImageFilePath,
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
                    propertyType
                )

                viewModel.insertGarbageCollectionInRoom(empGarbageCollectionRequest)
                viewModel.insertHouseOnMap(empHouseOnMap)

            } else if (viewModel.gcType != "1" && qrImageFilePath.isNotEmpty()) {
                val empGarbageCollectionRequest = EmpGarbageCollectionRequest(
                    0,
                    newLat,
                    newLong,
                    date,
                    viewModel.gcType,
                    userId!!,
                    referenceId,
                    qrImageFilePath,
                    propertyImageFilePath,
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
                    propertyType
                )

                viewModel.insertGarbageCollectionInRoom(empGarbageCollectionRequest)
                viewModel.insertHouseOnMap(empHouseOnMap)
            } else {
                CustomToast.showWarningToast(this, resources.getString(R.string.please_scan_again))
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
                pauseScanner()
                TurnOnGps.gpsStatusCheck(this, resolutionForResult)
            }
        }

        val snackBar = Snackbar.make(
            binding.parent,
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

    private val callback: BarcodeCallback = object : BarcodeCallback {

        override fun barcodeResult(result: BarcodeResult) {

            if (extractedQRCode != null) {
                if (result.text == null || result.text == extractedQRCode) {
                    // Prevent duplicate scans
                    return
                }
            }
            if (!isGpsOn) {
                pauseScanner()
                TurnOnGps.gpsStatusCheck(this@EmpQrScannerActivity, resolutionForResult)
            } else {
                extractedQRCode = result.text
                scannerView.setStatusText(extractedQRCode)
                beepManager.playBeepSoundAndVibrate()
                vibrateDevice(this@EmpQrScannerActivity)
                handleQrResult(extractedQRCode.toString())
            }

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isPhotoDialogVisible) {
            outState.putBoolean("IS_PHOTO_DIALOG_VISIBLE", true)
        }
    }

    private val mapActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            hideLoading()
            if (result.resultCode == Activity.RESULT_OK) {
                scannerView.pause()
                updatedLatitude = result.data!!.getStringExtra("updateLat")
                updatedLongitude = result.data!!.getStringExtra("updateLong")
                if (updatedLatitude != null && updatedLongitude != null) {
                    referenceId?.let {
                        val bundle = Bundle()
                        bundle.putString("updateLat", updatedLatitude)
                        bundle.putString("updateLong", updatedLongitude)
                        photoSubmitDialogFrag.arguments = bundle

                        binding.scannerView.transparentWhiteBg.visibility = View.VISIBLE
                        photoSubmitDialogFrag.languageId = languageId.toString()
                        if (viewModel.gcType == "1") {
                            photoSubmitDialogFrag.setPropertyTypeList(propertyTypeList)
                        }
                        photoSubmitDialogFrag.gcType = viewModel.gcType
                        photoSubmitDialogFrag.setReferenceId(it)
                        photoSubmitDialogFrag.show(
                            supportFragmentManager, PhotoSubmitDialogFrag.TAG
                        )
                    }

                    //  openCameraActivity.launch(CameraActivity.getIntent(this, 10))
                }
            } else {
                shouldResumeScanner = true
            }
        }

    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        if (activityResult.resultCode == RESULT_OK) {
            binding.scannerView.gpsProgressLayout.visibility = View.VISIBLE

            Handler(Looper.myLooper()!!).postDelayed({
                if (isGpsOn) {
                    binding.scannerView.gpsProgressLayout.visibility = View.GONE
                    resumeScanner()
                }

            }, 1000)

        } else if (activityResult.resultCode == RESULT_CANCELED) {
            // The user was asked to change settings, but chose not to
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()

        }
    }

    private fun vibrateDevice(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") it.vibrate(100)
            }
        }
    }

    private fun setUpBarcodeView() {

        // scannerView.setStatusText(resources.getString(R.string.qr_code_scanner_place_holder))
        scannerView.setStatusText("")
        val formats: Collection<BarcodeFormat> =
            listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)

        scannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        scannerView.initializeFromIntent(intent)
        scannerView.decodeContinuous(callback)
        beepManager = BeepManager(this)
    }


    override fun onResume() {
        super.onResume()


        if (referenceId == null) {
            resumeScanner()
        } else if (shouldResumeScanner) {
            resumeScanner()
        }

    }

    override fun onPause() {
        super.onPause()
        pauseScanner()

    }

    private fun pauseScanner() {
        binding.scannerView.qrScanner1.pause()
    }

    private fun resumeScanner() {
        binding.scannerView.qrScanner1.resume()
        scannerView.setStatusText("")
    }

    private fun initCameraPermission() {
        val cameraPermission = CameraPermission(this)
        cameraPermission.initCameraPermission()
    }

    private fun initVars() {


        lifecycleScope.launch {
            propertyTypeList = viewModel.propertyTypeList.first().toMutableList()
        }

        photoSubmitDialogFrag = PhotoSubmitDialogFrag()

        photoSubmitDialogFrag.setListener(this)

        //getting data from dashboard screen
        empType = intent.getStringExtra("empType")
        languageId = intent.getStringExtra("languageId")
        userId = intent.getStringExtra("userId")
        userTypeId = intent.getStringExtra("userTypeId")

        scannerView = binding.scannerView.qrScanner1
        userDataStore = UserDataStore(applicationContext)


    }

    private fun hasFlash(): Boolean {
        return applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }


    private fun setUpFlashFab() {

        binding.flashToggle.setOnClickListener {

            if (hasFlash()) {
                if (!isFlashLightOn) {
                    try {
                        // true sets the torch in ON mode
                        binding.flashToggle.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.ic_flash_on_indicator, null
                            )
                        )
                        scannerView.setTorchOn()

                    } catch (e: CameraAccessException) {
                        // prints stack trace on standard error
                        // output error stream
                        e.printStackTrace()
                    }
                    isFlashLightOn = true
                } else {

                    try {
                        // true sets the torch in OFF mode
                        binding.flashToggle.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.ic_flash_off, null
                            )
                        )
                        scannerView.setTorchOff()


                    } catch (e: CameraAccessException) {
                        // prints stack trace on standard error
                        // output error stream
                        e.printStackTrace()
                    }
                    isFlashLightOn = false
                }
            } else {
                binding.flashToggle.visibility = View.GONE
            }

        }
    }


    override fun finish() {
        super.finish()
        try {
            // true sets the torch in OFF mode

            binding.flashToggle.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_flash_off, null
                )
            )

            scannerView.setTorchOff()
            // Inform the user about the flashlight
            // status using Toast message

        } catch (e: CameraAccessException) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace()
        }
    }

    override fun onPhotoSubmitBtnClicked(
        referenceId: String,
        qrImageFilePath: String,
        propertyImageFilePath: String,
        dialog: Dialog,
        propertyType: Int
    ) {

        dialog.dismiss()
        if (viewModel.gcType != "1") {
            if ((referenceId.isNotEmpty() && referenceId != "") && (qrImageFilePath.isNotEmpty())) {
                submitEmpGc(referenceId, qrImageFilePath, propertyImageFilePath, propertyType)
            } else {
                CustomToast.showWarningToast(this, resources.getString(R.string.please_scan_again))
            }
        } else {
            if ((referenceId.isNotEmpty() && referenceId != "") && (qrImageFilePath.isNotEmpty() && propertyImageFilePath.isNotBlank())) {
                submitEmpGc(referenceId, qrImageFilePath, propertyImageFilePath, propertyType)
            } else {
                CustomToast.showWarningToast(this, resources.getString(R.string.please_scan_again))
            }
        }


    }

    override fun onDialogDismissed() {
        binding.scannerView.transparentWhiteBg.visibility = View.GONE

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}