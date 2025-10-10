package com.appynitty.kotlinsbalibrary.ghantagadi.ui.qrScanner

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.net.TrafficStats
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.ui.select_ulb_module.SelectUlb
import com.appynitty.kotlinsbalibrary.common.ui.userDetails.viewmodel.UserDetailsViewModel
import com.appynitty.kotlinsbalibrary.common.utils.AirplaneModeChangeReceiver
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.GpsStatusListener
import com.appynitty.kotlinsbalibrary.common.utils.LocationUtils
import com.appynitty.kotlinsbalibrary.common.utils.TurnOnGps
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.CustomAlertDialog
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.GarbageTypeDialogFragment
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.PopUpAlertDialog
import com.appynitty.kotlinsbalibrary.common.utils.permission.CameraPermission
import com.appynitty.kotlinsbalibrary.databinding.ActivityQrscannerBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "QRScannerActivity"

@AndroidEntryPoint
class QRScannerActivity : AppCompatActivity(), GarbageTypeDialogFragment.GarbageTypeDialogCallbacks,
    DialogInterface.OnClickListener {

    private val viewModel: QrScannerViewModel by viewModels()
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()

    private lateinit var binding: ActivityQrscannerBinding
    private lateinit var receiver: AirplaneModeChangeReceiver
    private lateinit var beepManager: BeepManager
    private lateinit var scannerView: DecoratedBarcodeView
    private var extractedQRCode: String? = null

    private var isAttendanceRequest: Boolean = false
    private var empType: String? = null
    private var userId: String? = null
    private var vehicleNumber: String? = null
    private var isGtFeatureOn: Boolean = false
    private var latitude: String? = null
    private var longitude: String? = null
    private var userTypeId: String? = null
    private var isInternetOn: Boolean = false
    private var isFlashLightOn = false
    private var dryImageFilePath: String? = null
    private var wetImageFilePath: String? = null
    private var beforeImagePath: String? = null
    private var afterImagePath: String? = null
    private var isGpsOn: Boolean = false
    private var offlineFirstImagePath: String? = null
    private var offlineSecondImagePath: String? = null
    private var languageId: String? = null
    private var comment: String? = null
    private var distance = "0"
    private var isDialogVisible = false


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isDialogVisible) {
            outState.putBoolean("SHOULD_FINISH_THE_ACTIVITY", true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrscannerBinding.inflate(layoutInflater)
        scannerView = binding.scannerView.qrScanner1
        setContentView(binding.root)

        if (savedInstanceState != null) {
            val shouldFinish = savedInstanceState.getBoolean("SHOULD_FINISH_THE_ACTIVITY")
            if (shouldFinish)
                finish()
        }

        initVars()

        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also {
            // registering the receiver
            // it parameter which is passed in  registerReceiver() function
            // is the intent filter that we have just created
            registerReceiver(receiver, it)
        }

        setUpFlashFab()
        setupPermissions()
        setUpBarcodeView()
        subscribeLiveData()
        subscribeChannelEvents()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    private fun subscribeChannelEvents() {

        lifecycleScope.launchWhenStarted {
            viewModel.qrScannerEventsFlow.collect { event ->

                when (event) {
                    QrScannerViewModel.QrScannerEvent.PauseScanner -> {
                        pauseScanner()
                    }

                    QrScannerViewModel.QrScannerEvent.ResumeScanner -> {
                        resumeScanner()
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowAlertDialog -> {
                        showAlertDialog(event.resourceId)
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowWarningMessage -> {
                        showWarningMessage(event.resourceId)
                    }

                    QrScannerViewModel.QrScannerEvent.ShowGarbageTypeDialog -> {
                        showGarbageTypeDialog()
                    }

                    is QrScannerViewModel.QrScannerEvent.SubmitScanQrData -> {
                        submitScannedQrData(event.garbageType, event.note, event.isDumpDirectSubmit)
                    }

                    QrScannerViewModel.QrScannerEvent.ClearImagePathFromDataStore -> {
                        clearImagePathFromDataStore()
                    }

                    QrScannerViewModel.QrScannerEvent.OpenDumpYardWeightActivityForResults -> {
                        startDumpWeightActivityForResults()
                    }

                    QrScannerViewModel.QrScannerEvent.HideLoading -> {
                        hideLoading()
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@QRScannerActivity, event.msg)
                    }

                    QrScannerViewModel.QrScannerEvent.ShowLoading -> {
                        showLoading()
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowResponseErrorMessage -> {
                        showApiErrorMessage(
                            event.msg, event.msgMr
                        )
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowResponseSuccessMessage -> {
                        showApiSuccessMessage(
                            event.msg, event.msgMr
                        )
                    }

                    QrScannerViewModel.QrScannerEvent.DeleteImages -> {
                        deleteImagesAfterUploaded()
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowSuccessDialog -> {
                        setSuccessPopUpDialog(event.referenceId)
                    }

                    QrScannerViewModel.QrScannerEvent.FinishActivity -> {
                        finish()
                    }

                    is QrScannerViewModel.QrScannerEvent.ShowSuccessToast -> {
                        showSuccessToast(event.resourceId)
                    }

                    QrScannerViewModel.QrScannerEvent.NavigateToLoginScreen -> {
                        LocationUtils.stopGisLocationTracking(applicationContext)
                        navigateToLoginScreen()
                    }
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        userDetailsViewModel.deleteAllUserDataFromRoom()
        val intent = Intent(this@QRScannerActivity, SelectUlb::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
        startAnotherActivity(intent)
        finish()
    }

    private fun startAnotherActivity(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
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

    private fun hideLoading() {
        binding.scannerView.internetSpeed.visibility = View.GONE
        binding.scannerView.progressBar.visibility = View.GONE
        binding.scannerView.transparentWhiteBg.visibility = View.GONE
        binding.flashToggle.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.scannerView.internetSpeed.visibility = View.VISIBLE
        binding.scannerView.progressBar.visibility = View.VISIBLE
        binding.scannerView.transparentWhiteBg.visibility = View.VISIBLE
        binding.flashToggle.visibility = View.INVISIBLE
    }

    private fun startDumpWeightActivityForResults() {
        val intent = Intent(this, DumpYardWeightActivity::class.java)
        intent.putExtra("REFERENCE_ID", viewModel.referenceId)
        openDumpWeightActivity.launch(intent)
    }

    private fun showGarbageTypeDialog() {

        val garbageTypePopUp = GarbageTypeDialogFragment()
        garbageTypePopUp.setListener(this)
        garbageTypePopUp.show(supportFragmentManager, GarbageTypeDialogFragment.TAG)
        //   garbageTypePopUp.isCancelable = false

        isDialogVisible = true
    }

    private fun showAlertDialog(resourceId: Int) {
        PopUpAlertDialog.showSimpleDialog(
            this, resources.getString(R.string.alert), resources.getString(resourceId), this
        )
    }

    private fun showWarningMessage(resourceId: Int) {
        CustomToast.showWarningToast(this, resources.getString(resourceId))
    }

    private fun initVars() {
        hideLoading()
        startNetworkSpeedMonitor { speed ->
            val formattedSpeed = "<b>$speed</b>"
            binding.scannerView.internetSpeed.text =
                HtmlCompat.fromHtml(
                    "Internet Speed: $formattedSpeed",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        }
        empType = intent.getStringExtra("empType")
        isAttendanceRequest = intent.getBooleanExtra("isAttendanceRequest",false)
        comment = intent.getStringExtra("comment")
        languageId = intent.getStringExtra("languageId")
        userId = intent.getStringExtra("userId")
        vehicleNumber = intent.getStringExtra("vehicleNumber")
        userTypeId = intent.getStringExtra("userTypeId")
        isGtFeatureOn = intent.getBooleanExtra("isGtFeatureOn", false)
        viewModel.isGtFeatureOn = isGtFeatureOn

        //when user is coming from take photo activity
        beforeImagePath = intent.getStringExtra("beforeImagePath")
        afterImagePath = intent.getStringExtra("afterImagePath")
        offlineFirstImagePath = beforeImagePath
        offlineSecondImagePath = afterImagePath

        receiver = AirplaneModeChangeReceiver()
        viewModel.getDeviceId(this)

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
        this.overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.referenceId == "") {
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


    private fun subscribeLiveData() {

        receiver.airplaneModeLiveData.observe(this) {

            if (!it) {
                TurnOnGps.gpsStatusCheck(this, resolutionForResult)
            } else {
//                CustomToast.showWarningToast(
//                    this@QRScannerActivity,
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
            viewModel.isInternetOn = it

            if (it) {
                snackBar.dismiss()
            } else {
                snackBar.show()
            }
        }
        viewModel.userLatLongFlow.asLiveData().observe(this) {
            latitude = it.latitude
            longitude = it.longitude
            distance = if (it.distance == "")
                "0"
            else
                it.distance
        }

    }

    private fun deleteImagesAfterUploaded() {

        var isDump = false

        wetImageFilePath?.let {
            isDump = true
            CameraUtils.deleteTheFile(it)
        }
        dryImageFilePath?.let {
            isDump = true
            CameraUtils.deleteTheFile(it)
        }

        if (beforeImagePath != null) {
            if (!isDump) {
                CameraUtils.deleteTheFile(beforeImagePath!!)
                viewModel.saveBeforeImagePath("")
            }
        }
        if (afterImagePath != null) {
            if (!isDump)
                CameraUtils.deleteTheFile(afterImagePath!!)
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

    private val openDumpWeightActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                var wetWeight: String? = null
                var dryWeight: String? = null

                if (result.data?.getStringExtra("wetWeight") != "null") {
                    wetWeight = result.data?.getStringExtra("wetWeight").toString()
                }
                if (result.data?.getStringExtra("dryWeight") != "null") {
                    dryWeight = result.data?.getStringExtra("dryWeight").toString()
                }
                val totalWeight = result.data?.getStringExtra("totalWeight").toString()
                dryImageFilePath = result.data?.getStringExtra("dryImageFilePath").toString()
                wetImageFilePath = result.data?.getStringExtra("wetImageFilePath").toString()
                val dumpId = result.data?.getStringExtra("referenceId").toString()

                offlineFirstImagePath = dryImageFilePath
                offlineSecondImagePath = wetImageFilePath

                if (dryImageFilePath == "null") {
                    offlineFirstImagePath = null
                }

                if (wetImageFilePath == "null") {
                    offlineSecondImagePath = null
                }

                if (totalWeight.isNotEmpty()) {

                    if (wetWeight == "null") {
                        wetWeight = "0.0"
                    }
                    if (dryWeight == "null") {
                        dryWeight = "0.0"
                    }

                    saveScannedQrData(
                        dumpId, viewModel.gcType, null, null, wetWeight, dryWeight, totalWeight
                    )

                    /** save dump yard trip when dump is scanned */
                    wetWeight?.let {
                        if (dryWeight != null) {
                            viewModel.saveDumpYardTrip(
                                it.toDouble(),
                                dryWeight.toDouble(),
                                totalWeight.toDouble(),
                                userId!!,
                                vehicleNumber!!
                            )
                        }
                    }
                }

            } else if (result.resultCode == RESULT_CANCELED) {
                finish()
            }
        }

    private fun submitScannedQrData(
        garbageType: String?,
        note: String?,
        isDumpDirectSubmit: Boolean
    ) {

        if (isDumpDirectSubmit) {
            saveScannedQrData(
                viewModel.referenceId, viewModel.gcType, garbageType, note, "0.0", "0.0", "0.0"
            )
        } else {
            if (note != null && note != "") {
                saveScannedQrData(
                    viewModel.referenceId, viewModel.gcType, garbageType, note, null, null, null
                )
            } else if (comment != null && comment != "") {
                saveScannedQrData(
                    viewModel.referenceId, viewModel.gcType, garbageType, comment, null, null, null
                )
            } else {
                saveScannedQrData(
                    viewModel.referenceId, viewModel.gcType, garbageType, null, null, null, null
                )
            }
        }

    }

    private fun setUpBarcodeView() {

        scannerView.setStatusText("")
        val formats: Collection<BarcodeFormat> =
            listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)

        scannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        scannerView.initializeFromIntent(intent)
        scannerView.decodeContinuous(callback)
        beepManager = BeepManager(this)
    }


    private val callback: BarcodeCallback = object : BarcodeCallback {

        override fun barcodeResult(result: BarcodeResult) {

            if (extractedQRCode != null) {
                if (result.text == null || result.text == extractedQRCode) {
                    // Prevent duplicate scans
                    return
                }
            }
            Log.e(
                TAG, "barcodeResult: " + result.text + ", Bitmap: " + result.bitmap
            )
            if (!isGpsOn) {
                pauseScanner()
                TurnOnGps.gpsStatusCheck(this@QRScannerActivity, resolutionForResult)
            } else {
                extractedQRCode = result.text
                scannerView.setStatusText(result.text)
                beepManager.playBeepSoundAndVibrate()
                handleQrResult(result)
            }

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    fun handleQrResult(result: BarcodeResult) {

        vibrateDevice(this)

        if (isAttendanceRequest) {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("QrResult", result.text)
            setResult(RESULT_OK, intent)
            finish()
        }
        else {
            empType?.let { viewModel.validateScannedQrCode(it, result.text) }
        }
    }


    override fun onSubmitGarbageTypeDialog(garbageType: String?, note: String?) {

        viewModel.garbageTypeDialogSubmitClicked(garbageType, note)

    }

    override fun onDialogDismiss() {
        finish()
    }

    // if image is uploaded offline , we need to remove it from datastore other wise it will be shown in take photo activity
    private fun clearImagePathFromDataStore() {
        if (beforeImagePath != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.saveBeforeImagePath("")
            }
        }
    }

    private fun setSuccessPopUpDialog(referenceId: String?) {

        isDialogVisible = true

        val view = CustomAlertDialog.showSimpleDialog(this)
        val houseOwnerName = view.findViewById<TextView>(R.id.house_owner_name)
        val houseTitle = view.findViewById<TextView>(R.id.lbl_title)
        val collectionStatus = view.findViewById<TextView>(R.id.collection_status)
        val doneBtn = view.findViewById<Button>(R.id.done_btn)

        //set data to views
        houseOwnerName.text = referenceId

        if (viewModel.submitDialogTitleText == "Dump yard Id" || viewModel.submitDialogTitleText == "Vehicle Id") collectionStatus.text =
            resources.getString(R.string.collectionStatus)

        houseTitle.text = viewModel.submitDialogTitleText

        doneBtn.setOnClickListener {
            CustomAlertDialog.hideSimpleDialog()
            finish()
        }
    }

    private fun saveScannedQrData(
        referenceId: String,
        gcType: String,
        garbageType: String?,
        note: String?,
        wetWeight: String?,
        dryWeight: String?,
        totalWeight: String?,
    ) {

        /** trip blockchain */

        garbageType?.let { viewModel.insertTripHouse(it) }

        val batteryStatus = CommonUtils.getBatteryStatus(application).toString()
        val garbageCollectionData: GarbageCollectionData?
        val intBatteryStatus = batteryStatus.toInt()

        if (isInternetOn) {

            val beforeAfterImagesMap = CameraUtils.prepareBeforeAfterImages(
                offlineFirstImagePath,
                offlineSecondImagePath,
                referenceId,
                latitude!!,
                longitude!!,
                DateTimeUtils.getSimpleDateTime()
            )

            var beforeImageBase64: String? = null
            var afterImageBase64: String? = null

            if (offlineFirstImagePath != null) beforeImageBase64 =
                beforeAfterImagesMap["beforeImageBase64"]

            if (offlineSecondImagePath != null) afterImageBase64 =
                beforeAfterImagesMap["afterImageBase64"]

            garbageCollectionData = GarbageCollectionData(
                0,
                referenceId,
                userId!!,
                latitude!!,
                longitude!!,
                vehicleNumber!!,
                gcType,
                garbageType,
                DateTimeUtils.getScanningServerDate(),
                batteryStatus,
                distance,
                isLocation = false,
                isOffline = false,
                empType!!,
                note,
                beforeImageBase64,
                afterImageBase64,
                null,
                totalWeight,
                dryWeight,
                wetWeight
            )

            Log.d(TAG, "saveScannedQrData: $garbageCollectionData")
            viewModel.saveGarbageCollectionOnlineDataToApi(
                CommonUtils.APP_ID,
                userTypeId!!,
                intBatteryStatus,
                CommonUtils.CONTENT_TYPE,
                garbageCollectionData
            )

        } else {
            garbageCollectionData = GarbageCollectionData(
                0,
                referenceId,
                userId!!,
                latitude!!,
                longitude!!,
                vehicleNumber!!,
                gcType,
                garbageType,
                DateTimeUtils.getScanningServerDate(),
                batteryStatus,
                distance,
                isLocation = false,
                isOffline = true,
                empType = empType!!,
                note = note,
                gpBeforeImage = offlineFirstImagePath,
                gpAfterImage = offlineSecondImagePath,
                gpBeforeImageTime = null,
                totalGcWeight = totalWeight,
                totalDryWeight = dryWeight,
                totalWetWeight = wetWeight
            )
            viewModel.saveGarbageCollectionOffline(garbageCollectionData)
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

    private fun setupPermissions() {
        val cameraPermission = CameraPermission(this)
        cameraPermission.initCameraPermission()
    }

    //when user scans different qr code : dismiss dialog when user clicks on ok btn
    override fun onClick(dialog: DialogInterface?, which: Int) {
        dialog?.dismiss()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun startNetworkSpeedMonitor(onSpeedUpdate: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var previousRxBytes = TrafficStats.getTotalRxBytes()
            var previousTxBytes = TrafficStats.getTotalTxBytes()

            while (true) {
                delay(1000) // every second
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()

                val downloadSpeed = (currentRxBytes - previousRxBytes) * 8 / 1024 // Kbps
                val uploadSpeed = (currentTxBytes - previousTxBytes) * 8 / 1024 // Kbps

                previousRxBytes = currentRxBytes
                previousTxBytes = currentTxBytes

                withContext(Dispatchers.Main) {
                    onSpeedUpdate("↓ $downloadSpeed Kbps | ↑ $uploadSpeed Kbps")
                }
            }
        }
    }
}