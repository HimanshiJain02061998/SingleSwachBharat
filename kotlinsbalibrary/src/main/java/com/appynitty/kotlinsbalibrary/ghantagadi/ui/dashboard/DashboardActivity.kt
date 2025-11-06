package com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.location.GisLocationService
import com.appynitty.kotlinsbalibrary.common.location.awaitCurrentLocation
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.AddUlbActivity
import com.appynitty.kotlinsbalibrary.common.ui.my_location.MyLocationActivity
import com.appynitty.kotlinsbalibrary.common.ui.privacyPolicy.PrivacyPolicyActivity
import com.appynitty.kotlinsbalibrary.common.ui.profile.ProfileActivity
import com.appynitty.kotlinsbalibrary.common.ui.userDetails.viewmodel.UserDetailsViewModel
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.GpsStatusListener
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.LocationUtils
import com.appynitty.kotlinsbalibrary.common.utils.TurnOnGps
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserVehicleDetails
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.AlertMessageDialogFrag
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.LanguageBottomSheetFrag
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.PopUpDialogFragment
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.SettingBottomSheetFrag
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.SettingTeamsBottomSheetFrag
import com.appynitty.kotlinsbalibrary.common.utils.permission.LocationPermission
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityDashboardBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.TripRepository
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.InPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.OutPunchRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AvailableEmpItem
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DumpYardIds
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.VehicleTypeResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule.EmployeeViewModel
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.addMemberModule.SelectMembers
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.qrScanner.QRScannerActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.GarbageCollectionViewModel
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.GarbageCollectionViewModelFactory
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.SyncOfflineActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.takePhoto.TakePhotoActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory.WorkHistoryActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 *  created by sanath gosavi
 */
private const val TAG = "DashboardActivity"

@Suppress("DEPRECATION")
@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), DashboardAdapter.MenuItemClickedInterface,
    PopUpDialogFragment.PopUpDialogFragmentClickListeners,
    LanguageBottomSheetFrag.LanguageDialogCallbacks, SettingBottomSheetFrag.SettingsCallBack,
    AlertMessageDialogFrag.AlertMessageDialogCallBacks, SettingTeamsBottomSheetFrag.SettingsTeamCallBack {

    private val viewModel: DashboardViewModel by viewModels()
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()
    private val employeeViewModel: EmployeeViewModel by viewModels()
    //garbage viewModel has an application scope as it is used for syncing functionality
    //it is required in two activities dashboard and sync offline
    @Inject
    lateinit var garbageCollectionDao: GarbageCollectionDao

    @Inject
    lateinit var archivedDao: ArchivedDao
    @Inject
    lateinit var tripRepository: TripRepository
    @Inject
    lateinit var sessionDataStore : SessionDataStore

    @Inject
    lateinit var garbageCollectionRepo: GarbageCollectionRepo
    private lateinit var garbageCollectionViewModel: GarbageCollectionViewModel

    private lateinit var binding: ActivityDashboardBinding

    private lateinit var dialog: PopUpDialogFragment
    private var userId: String? = null
    private var empType: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var userTypeId: String? = null
    private var vehicleNumber: String? = null
    private var isDutyOn = false
    private var isUserLogin = false
    private var userData: UserData? = null
    private var isAllFabVisible = false
    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var languageBottomSheet: LanguageBottomSheetFrag
    private lateinit var languageDataStore: LanguageDataStore
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private var userVehicleDetailsTemp: UserVehicleDetails? = null
    private var userVehicleDetailsDataStore: UserVehicleDetails? = null
    private var isBifurcationOn = true
    private var isTeamSelected = true
    private var isVehicleScanOn = false
    private lateinit var settingsBottomSheet: SettingBottomSheetFrag
    private lateinit var settingsTeamBottomSheet: SettingTeamsBottomSheetFrag
    private var isGpsOn = false
    private var isDutyToggleChecked = false
    private var isInternetOn = true
    private lateinit var locationPermission: LocationPermission
    private lateinit var syncSnackBar: Snackbar
    private var isSyncingOn = false
    private lateinit var alertMessageDialogFrag: AlertMessageDialogFrag
    private var isDutyOnToggleClicked = false
    private lateinit var selectedEmployeeSpinner: Spinner
    private var selectedTeamMembers: List<AvailableEmpItem>? = null

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val msg = intent?.getStringExtra("message")
            if (msg=="LOG_OUT"){
                viewModel.performForcefullyLogout()
                showApiErrorMessage("Invalid IMEI No", "अवैध IMEI No")
//             viewModel.shouldStartLocationService(
//                 isMyServiceRunning(
//                     GisLocationService::class.java
//                 )
//             )
//             stopLocationTracking()
            }
        }
    }


    private val vehicleQrLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if(result.data?.extras != null){
                val liquidQr = result.data?.extras?.getString("QrResult")
                if (liquidQr != null)
                    useTheResult(liquidQr)
            }else{
                enableDutyToggle()
            }
        }
    private val selectMembersLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                selectedTeamMembers =
                    result.data?.getParcelableArrayListExtra("SELECTED_MEMBERS")

                val selectedMemberIds = selectedTeamMembers?.map { it.userid }
                val userVehicleDetails = UserVehicleDetails("1", "", "1")

                val inPunchRequest = InPunchRequest(
                    DateTimeUtils.getServerTime(),
                    DateTimeUtils.getYyyyMMddDate(),
                    latitude.toString(),
                    longitude.toString(),
                    userId,
                    "1",
                    "1",
                    empType,
                    "",
                    selectedMemberIds
                )

                selectedMemberIds?.let {
                    viewModel.saveInPunchLiquid(
                        userId = userId,
                        batteryStatus = CommonUtils.getBatteryStatus(application),
                        memberUserIds = it,
                        latitude = latitude?.toDouble(),
                        longitude = longitude?.toDouble(),
                        inPunchRequest = inPunchRequest,
                        userVehicleDetails = userVehicleDetails
                    )
                }
                viewModel.setTeamSelected(true)

            } else {
                enableDutyToggle()
            }
        }


    private fun useTheResult(liquidQr: String?) {

        if (latitude == null && longitude == null) {
            CustomToast.showWarningToast(this, "Couldn't find location")
        } else {

            val inPunchRequest = InPunchRequest(
                DateTimeUtils.getServerTime(),
                DateTimeUtils.getYyyyMMddDate(),
                latitude!!,
                longitude!!,
                userId,
                "",
                "",
                empType,
                liquidQr
            )

            viewModel.getVehicleQrDetails(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                liquidQr!!,
                empType!!,
                latitude!!,
                longitude!!,
                CommonUtils.getBatteryStatus(application),
                inPunchRequest
            )

        }
    }


    //multi language functionality
    override fun attachBaseContext(newBase: Context?) {

        newBase?.let { context ->
            languageDataStore = LanguageDataStore(newBase.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            selectedLanguage = appLanguage.languageName
            selectedLanguageId = appLanguage.languageId
            LanguageConfig.changeLanguage(context, selectedLanguageId.toString())
        }
        super.attachBaseContext(newBase)
    }

    private fun restartActivity() {

        showProgressBar()
        val intent = intent

        lifecycleScope.launch {

            delay(800)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)

        }
    }

    override fun onResume() {
        super.onResume()

        isDutyOnToggleClicked = false
        viewModel.checkIsDateChanged()
        setUpGridRecyclerView()
    }

    //checking if service is running or not
    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        initToolBar()
        getInstantLocation()
        getUserDetailsFromRoom()
        subscribeLiveData()
        subscribeChannelEvents()
        registerClickedEvents()
        setUpFabBtn()
        handleBackBtnInDashboard()
    }



    private fun subscribeChannelEvents() {

        /// getting channel events from viewModel ( single time events )
        lifecycleScope.launchWhenStarted {
            viewModel.dashboardEventsFlow.collect { event ->

                when (event) {

                    is DashboardViewModel.DashboardEvent.NavigateToQrScreen -> {
                        navigateToQrScreen(event.isGtFeatureOn)
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToProfileScreen -> {
                        navigateToProfileScreen()
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToSyncOfflineScreen -> {
                        navigateToSyncOfflineScreen()
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToTakePhotoScreen -> {
                        navigateToTakePhotoScreen()
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToWorkHistoryScreen -> {
                        navigateToWorkHistoryScreen()
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToLoginScreen -> {
                        navigateToLoginScreen()
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToSelectUlbScreen -> {
                        navigateToSelectUlbScreen()
                    }

                    is DashboardViewModel.DashboardEvent.ShowWarningMessage -> {
                        CustomToast.showWarningToast(
                            this@DashboardActivity, resources.getString(event.resourceId)
                        )
                    }

                    is DashboardViewModel.DashboardEvent.NavigateToPrivacyPolicyScreen -> {
                        navigateToPrivacyPolicyScreen()
                    }

                    is DashboardViewModel.DashboardEvent.TurnGpsOn -> {
                        TurnOnGps.gpsStatusCheck(this@DashboardActivity, resolutionForResult)
                    }

                    is DashboardViewModel.DashboardEvent.EnableDutyToggle -> {
                        enableDutyToggle()
                    }

                    is DashboardViewModel.DashboardEvent.TurnDutyOff -> {
                        turnDutyOff()
                    }

                    is DashboardViewModel.DashboardEvent.ShowSettingScreen -> {
                        showSettingsBottomSheet()
                    }

                    is DashboardViewModel.DashboardEvent.ShowChangeLanguageScreen -> {
                        showChangeLanguageBottomSheet()
                    }

                    is DashboardViewModel.DashboardEvent.ShowAlertDialog -> {
                        showAlertDialog(
                            event.titleResourceId, event.messageResourceId, event.dialogType
                        )
                    }

                    is DashboardViewModel.DashboardEvent.DismissAlertDialogFrag -> {
                        dismissAlertDialog()
                    }

                    is DashboardViewModel.DashboardEvent.RestartDashboardActivity -> {
                        restartActivity()
                    }

                    is DashboardViewModel.DashboardEvent.DismissLanguageDialog -> {
                        languageBottomSheet.dismiss()
                    }

                    is DashboardViewModel.DashboardEvent.GetVehiclesData -> {
                        isDutyOnToggleClicked = true
                        getVehicleTypeDetails()
                    }

                    is DashboardViewModel.DashboardEvent.ShowProgressBar -> {
                        showProgressBar()
                    }

                    is DashboardViewModel.DashboardEvent.ShowVehicleTypeDialog -> {
                        showVehicleTypeDialog(event.vehicleTypeList)
                    }

                    is DashboardViewModel.DashboardEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@DashboardActivity, event.msg)
                    }

                    is DashboardViewModel.DashboardEvent.ShowVehicleNumberList -> {
                        if (!dialog.isDetached) {
                            event.vehicleNumberList.let { it1 ->
                                dialog.setVehicleNumberList(it1)
                            }
                        }
                    }

                    DashboardViewModel.DashboardEvent.HideProgressBar -> {
                        hideProgressBar()
                    }

                    DashboardViewModel.DashboardEvent.SaveVehicleDetails -> {
                        saveUserVehicleDetails()
                    }

                    is DashboardViewModel.DashboardEvent.ShowResponseErrorMessage -> {
                        showApiErrorMessage(event.msg, event.msgMr)
                    }

                    is DashboardViewModel.DashboardEvent.ShowResponseSuccessMessage -> {
                        showApiSuccessMessage(event.msg, event.msgMr)
                    }

                    DashboardViewModel.DashboardEvent.StartLocationTracking -> {
                        startLocationTracking()
                    }

                    DashboardViewModel.DashboardEvent.StopLocationTracking -> {
                        stopLocationTracking()
                    }

                    DashboardViewModel.DashboardEvent.MakeDutyToggleClickedFalse -> {
                        isDutyOnToggleClicked = false
                    }

                    DashboardViewModel.DashboardEvent.CheckIfServiceIsRunning -> {
                        viewModel.shouldStartLocationService(
                            isMyServiceRunning(
                                GisLocationService::class.java
                            )
                        )
                    }

                    DashboardViewModel.DashboardEvent.HitGisServer -> {
                        // viewModel.hitGisServerWhenDutyOff(userId!!)
                    }

                    DashboardViewModel.DashboardEvent.HideDialogProgressBar -> {
                        if (dialog.isAdded) {
                            dialog.hideProgressBar()
                        }
                    }

                    DashboardViewModel.DashboardEvent.ShowDialogProgressBar -> {

                        if (dialog.isAdded) {
                            dialog.showProgressBar()
                        }
                    }

                    DashboardViewModel.DashboardEvent.StartVehicleQrScanner -> {

                        val intent = Intent(
                            this@DashboardActivity,
                            QRScannerActivity::class.java
                        )
                        intent.putExtra("isAttendanceRequest", true)
                        vehicleQrLauncher.launch(
                            intent
                        )
                    }

                    is DashboardViewModel.DashboardEvent.ShowDumpYardIdsDialog -> {
                        showDumpYardIdsDialog(event.dumpYardIdsList)
                    }

                    DashboardViewModel.DashboardEvent.NavigateToMyLocationScreen -> {
                        val mapsIntent =
                            Intent(this@DashboardActivity, MyLocationActivity::class.java)
                        if (latitude != null && latitude?.isNotEmpty() == true) {
                            mapsIntent.putExtra("latitude", latitude?.toDouble())
                            mapsIntent.putExtra("longitude", longitude?.toDouble())

                        }
                        startActivity(mapsIntent)
                    }

                    DashboardViewModel.DashboardEvent.UserDetailsUpdate -> {
                        // User Details Update
                        getUserDetailsUpdateFromApi()

                    }

                    DashboardViewModel.DashboardEvent.ShowLiquidEmployeeDialog -> {
                     //   showSelectTypeDialog()
                        val mapsIntent =
                            Intent(this@DashboardActivity, SelectMembers::class.java)
                        mapsIntent.putExtra("USER_ID", userId)
                        selectMembersLauncher.launch(mapsIntent)
                    }

                    is DashboardViewModel.DashboardEvent.TeamON -> {
                        viewModel.setTeamSelected(true)
                    }

                    DashboardViewModel.DashboardEvent.ShowSettingTeamScreen -> {
                        if (isDutyOn) {
                            CustomToast.showWarningToast(
                                this@DashboardActivity,
                                getString(R.string.off_duty_warning)
                            )
                        } else
                            showSettingsTeamBottomSheet()
                    }
                }

            }
        }
        lifecycleScope.launchWhenStarted {
            garbageCollectionViewModel.garbageCollectionEventsFlow.collect { event ->
                when (event) {
                    GarbageCollectionViewModel.LogoutEvent.PerformForcefullyLogout -> {
                        viewModel.performForcefullyLogout()
                    }

                    is GarbageCollectionViewModel.LogoutEvent.ShowResponseErrorMessage -> {
                        showApiErrorMessage(event.msg, event.msgMr)
                    }
                }
            }
        }
    }

    private fun showSelectTypeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_type, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupType)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            enableDutyToggle()
            dialog.dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                CustomToast.showWarningToast(this, getString(R.string.please_select_one_option))
                return@setOnClickListener
            }

            when (selectedId) {
                R.id.radioSingle -> {
                    viewModel.setTeamSelected(false)
                    val inPunchRequest = userId?.let {
                        empType?.let { employeeType ->
                            InPunchRequest(
                                DateTimeUtils.getServerTime(),
                                DateTimeUtils.getYyyyMMddDate(),
                                latitude!!,
                                longitude!!,
                                it,
                                "1",
                                "1",
                                employeeType,

                                )
                        }
                    }
                    inPunchRequest?.let { it1 ->
                        viewModel.saveInPunchLiquid(
                            userId = userId,
                            batteryStatus = CommonUtils.getBatteryStatus(application),
                            memberUserIds = emptyList(),
                            latitude = latitude?.toDoubleOrNull(),
                            longitude = longitude?.toDoubleOrNull(),
                            it1,
                            null
                        )
                    }
                }

                R.id.radioTeam -> {
                    val mapsIntent =
                        Intent(this@DashboardActivity, SelectMembers::class.java)
                    mapsIntent.putExtra("USER_ID", userId)
                    selectMembersLauncher.launch(mapsIntent)
                }
            }

            dialog.dismiss()
        }
    }

    private fun showSelectedTeamDialog() {
        if (selectedTeamMembers.isNullOrEmpty()) {
            CustomToast.showWarningToast(this, getString(R.string.no_team_members_selected))
            return
        }

        val memberNames = selectedTeamMembers!!.joinToString("\n") { member ->
            "- ${member.EmployeeName ?: "Unknown"}"
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.selected_team_members))
            .setMessage(memberNames)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun startLocationTracking() {

        if (isIgnoringBatteryOptimizations()) {
            LocationUtils.startGisLocationTracking(this)
        } else {
            batteryOptimizationRequest()
        }
    }

    private fun stopLocationTracking() {
        LocationUtils.stopGisLocationTracking(this)
    }

    private fun showApiSuccessMessage(msg: String?, msgMr: String?) {
        if (selectedLanguageId == "mr") {
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
        if (selectedLanguageId == "mr") {
            msgMr?.let {
                CustomToast.showErrorToast(this, msgMr)
            }
        } else {
            msg?.let {
                CustomToast.showErrorToast(this, msg)
            }
        }
    }

    private fun saveUserVehicleDetails() {
        if (userVehicleDetailsTemp != null)
            viewModel.saveUserVehicleDetails(userVehicleDetailsTemp!!)
    }

    private fun navigateToLoginScreen() {
        garbageCollectionViewModel.setSyncingLiveDataToNull()
        userDetailsViewModel.deleteAllUserDataFromRoom()
        val intent = Intent(this@DashboardActivity, AddUlbActivity::class.java)
        startAnotherActivity(intent)
        finish()

    }

    private fun navigateToSelectUlbScreen() {

        userDetailsViewModel.deleteAllUserDataFromRoom()
        val intent = Intent(this@DashboardActivity, AddUlbActivity::class.java)
        startAnotherActivity(intent)
        finish()

    }

    private fun navigateToWorkHistoryScreen() {
        val intent = Intent(this@DashboardActivity, WorkHistoryActivity::class.java)
        intent.putExtra("empType", empType)
        intent.putExtra("userId", userId)
        startAnotherActivity(intent)
    }

    private fun navigateToTakePhotoScreen() {
        val intent = Intent(this@DashboardActivity, TakePhotoActivity::class.java)
        intent.putExtra("empType", empType)
        intent.putExtra("userId", userId)
        intent.putExtra("userTypeId", userTypeId)
        intent.putExtra("vehicleNumber", vehicleNumber)
        intent.putExtra("languageId", selectedLanguageId)
        startAnotherActivity(intent)
    }

    private fun navigateToSyncOfflineScreen() {
        Log.i("SyncOfflineClicked", "navigateToSyncOfflineScreen: clicked")
        val intent = Intent(this@DashboardActivity, SyncOfflineActivity::class.java)
        intent.putExtra("empType", empType)
        intent.putExtra("userTypeId", userTypeId)
        intent.putExtra("languageId", selectedLanguageId)
        startAnotherActivity(intent)
    }

    private fun navigateToQrScreen(isGtFeatureOn: Boolean) {
        val intent = Intent(this@DashboardActivity, QRScannerActivity::class.java)
        intent.putExtra("empType", empType)
        intent.putExtra("userId", userId)
        intent.putExtra("userTypeId", userTypeId)
        intent.putExtra("vehicleNumber", vehicleNumber)
        intent.putExtra("languageId", selectedLanguageId)
        intent.putExtra("isGtFeatureOn", isGtFeatureOn)
        startAnotherActivity(intent)
    }

    private fun navigateToProfileScreen() {
        val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
        if (userData != null) {
            intent.putExtra("userData", userData)
        }
        startAnotherActivity(intent)
    }

    private fun startAnotherActivity(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )
    }

    private fun enableDutyToggle() {
        binding.userAttendanceToggle.isEnabled = true
    }

    private fun disableDutyToggle() {
        binding.userAttendanceToggle.isEnabled = false
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showVehicleTypeDialog(list: List<VehicleTypeResponse>) {
        list.let {
            val arrayList: ArrayList<VehicleTypeResponse> = ArrayList()
            arrayList.addAll(it)
            dialog.setVehicleTypeList(arrayList)
        }
        if (!dialog.isAdded)
            dialog.show(supportFragmentManager, PopUpDialogFragment.TAG)
    }

    private fun showDumpYardIdsDialog(list: List<DumpYardIds>) {

        list.let {
            val mList = ArrayList<String>()
            list.forEach {
                mList.add(it.dyId)
            }
            dialog.setDumpYardIds(mList)
        }


        if (!dialog.isAdded)
            dialog.show(supportFragmentManager, PopUpDialogFragment.TAG)
    }


    private fun getVehicleTypeDetails() {

        viewModel.getVehicleTypeDetails()

    }

    private fun navigateToPrivacyPolicyScreen() {

        showProgressBar()

        val intent = Intent(this@DashboardActivity, PrivacyPolicyActivity::class.java)
        startAnotherActivity(intent)

        lifecycleScope.launch {
            delay(50)

            //shrinkFab()
            hideProgressBar()
        }
    }

    private fun subscribeLiveData() {

        viewModel.isBifurcationOnLiveData.observe(this, Observer {
            isBifurcationOn = it
        })

        viewModel.isTeamSelected.observe(this, Observer {
            isTeamSelected = it
        })

        viewModel.isVehicleScanOnLiveData.observe(this, Observer {
            isVehicleScanOn = it
        })

        viewModel.isUserLoggedInLiveData.observe(this, Observer {
            isUserLogin = it
        })
        viewModel.userLocationLiveData.observe(this, Observer {
            latitude = it.latitude
            longitude = it.longitude
        })
        ConnectivityStatus(this).observe(this, Observer {
            isInternetOn = it
            if (isInternetOn) {
                submitOfflineData()
            }
        })

        GpsStatusListener(this).observe(this, Observer {
            isGpsOn = it
        })

        garbageCollectionViewModel.garbageCollectionResponseLiveData.observe(
            this,
            Observer {
                when (it) {
                    is ApiResponseListener.Loading -> {}
                    is ApiResponseListener.Success -> {
                        CustomToast.showSuccessToast(this, "Batch Synced Successfully")
                    }

                    is ApiResponseListener.Failure -> {
                        CustomToast.showErrorToast(this, it.message.toString())
                    }

                    null -> {
                        //take care
                    }
                }
            })

        viewModel.userVehicleDetailsFlow.observe(this, Observer {

            vehicleNumber = it.vehicleNumber
            userVehicleDetailsDataStore = it


            if (it.vehicleTypeName != "") {
                binding.userVehicleType.text = buildString {
                    append("(")
                    append(it.vehicleTypeName)
                    append(" - ")
                    append(vehicleNumber)
                    append(")")
                }
            } else {
                if (empType == "D") {
                    binding.userVehicleType.text = buildString {
                        append("(")
                        append(resources.getString(R.string.dump_yard_id_txt))
                        append(" - ")
                        append(vehicleNumber)
                        append(")")
                    }
                }
            }

            Log.i(
                "VEHICLE_TYPE_CHECK",
                "subscribeLiveData: ${binding.userVehicleType.text}"
            )

        })

        viewModel.isUserDutyOnFlow.asLiveData().observe(this, Observer {
            isDutyOn = it
            enableDutyToggle()
            binding.userAttendanceToggle.isChecked = it

            if (isDutyOn) {
                binding.userAttendanceStatus.setTextColor(
                    resources.getColor(
                        R.color.colorONDutyGreen, null
                    )
                )
                binding.userAttendanceStatus.text =
                    resources.getString(R.string.status_on_duty)
                binding.userVehicleType.visibility = View.VISIBLE
            } else {
                binding.userAttendanceStatus.setTextColor(
                    resources.getColor(
                        R.color.colorOFFDutyRed, null
                    )
                )
                binding.userAttendanceStatus.text =
                    resources.getString(R.string.status_off_duty)
                binding.userVehicleType.text = ""
                binding.userVehicleType.visibility = View.INVISIBLE
            }
        })

        garbageCollectionViewModel.isSyncingOnLiveData.observe(this, Observer {

            isSyncingOn = it
            if (it) {
                syncSnackBar.show()
            } else {
                syncSnackBar.dismiss()
            }

        })


        userDetailsViewModel.userDetailsLiveData.observe(this, Observer {
            when (it) {

                is ApiResponseListener.Success -> {
                    hideProgressBar()
                    getUserDetailsFromRoom()

                }

                is ApiResponseListener.Loading -> {
                    showProgressBar()
                }

                is ApiResponseListener.Failure -> {
                    hideProgressBar()
                }

            }
        })
    }

    @SuppressLint("BatteryLife")
    private fun batteryOptimizationRequest() {
        val intent = Intent()
        val packageName = this.applicationContext.packageName
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:${getPackageName()}")
            this.startActivity(intent)
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager =
            this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = this.applicationContext.packageName
        return powerManager.isIgnoringBatteryOptimizations(name)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initVars() {
        /*   startNetworkSpeedMonitor { speed ->
               binding.internetSpeed.text = "Internet Speed: $speed"
           }*/
        locationPermission = LocationPermission(this)
        locationPermission.initFineLocationPermission(this)

        alertMessageDialogFrag = AlertMessageDialogFrag()
        alertMessageDialogFrag.setListener(this)
        settingsBottomSheet = SettingBottomSheetFrag()
        settingsTeamBottomSheet = SettingTeamsBottomSheetFrag()
        languageBottomSheet = LanguageBottomSheetFrag()

        val garbageCollectionViewModelFactory = GarbageCollectionViewModelFactory(
            application,
            garbageCollectionRepo,
            garbageCollectionDao,
            archivedDao,
            tripRepository,
            sessionDataStore
        )

        garbageCollectionViewModel = ViewModelProvider(
            MyApplication.instance,
            garbageCollectionViewModelFactory
        )[GarbageCollectionViewModel::class.java]

        syncSnackBar = Snackbar.make(
            binding.nestedCoordinatorLayout,
            resources.getString(R.string.syncing_is_on),
            Snackbar.LENGTH_INDEFINITE
        )

        syncSnackBar.view.background =
            ResourcesCompat.getDrawable(resources, R.drawable.snackbar_bg, null)

        dialog = PopUpDialogFragment()
        dialog.setListener(this)
        viewModel.getDeviceId(this)
        garbageCollectionViewModel.getDeviceId(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                serviceReceiver,
                IntentFilter("com.appynitty.LOGOUT_EVENT"),
                Context.RECEIVER_NOT_EXPORTED // or RECEIVER_EXPORTED based on your use case
            )
        } else {
            registerReceiver(
                serviceReceiver,
                IntentFilter("com.appynitty.LOGOUT_EVENT")
            )
        }

        viewModel.isTeamSelected.observe(this, Observer {
            if (it == true && isDutyOn) {
                binding.viewTeamButton.visibility = View.VISIBLE
            }else{
                binding.viewTeamButton.visibility = View.GONE
            }
            if(it == true){
                binding.teamNote.visibility = View.VISIBLE
            }else{
                binding.teamNote.visibility = View.GONE
            }
        })

        viewModel.teamMembersSelected.observe(this, Observer {
            if(it != null){
                selectedTeamMembers = it

            }
        })

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun registerClickedEvents() {

        binding.userAttendanceToggle.setOnClickListener {

            isDutyToggleChecked = binding.userAttendanceToggle.isChecked
            getInstantLocation()
            binding.userAttendanceToggle.isChecked = isDutyOn
            disableDutyToggle()

            viewModel.onDutyToggleClicked(
                isInternetOn,
                isGpsOn,
                isDutyToggleChecked,
                isVehicleScanOn,
                userId,
                CommonUtils.getBatteryStatus(application)
            )

        }

        binding.userAttendanceToggle.setOnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }

        binding.viewTeamButton.setOnClickListener {
            showSelectedTeamDialog()
        }
    }

    private fun turnDutyOff() {

        if (latitude == null && longitude == null) {
            CustomToast.showWarningToast(this, "Couldn't find location")
        } else {
            val outPunchRequest = userId?.let {
                empType?.let { it1 ->
                    OutPunchRequest(
                        DateTimeUtils.getServerTime(),
                        DateTimeUtils.getYyyyMMddDate(),
                        latitude!!,
                        longitude!!,
                        it,
                        userVehicleDetailsDataStore?.vehicleId,
                        userVehicleDetailsDataStore?.vehicleNumber,
                        it1
                    )
                }
            }
            Log.d(TAG, "saveOutPunchDetails: $outPunchRequest")
            if (outPunchRequest != null) {
                viewModel.saveOutPunchDetails(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    CommonUtils.getBatteryStatus(application),
                    outPunchRequest
                )
                binding.viewTeamButton.visibility = View.GONE
            }
        }
    }

    private fun submitOfflineData() {

        //sync dump yard trip blockchain
//        if (isInternetOn)
//            garbageCollectionViewModel.syncDumpYardTrip()

        if (!isSyncingOn) {
            lifecycleScope.launch {

                if (viewModel.checkSameUserLogin()) {
                    Log.d("tempId", "Temp id is ${viewModel.checkSameUserLogin()}")

                    val gcCount = garbageCollectionViewModel.getGcCount()
                    if (gcCount > 0) {
                        userTypeId?.let {
                            garbageCollectionViewModel.saveGarbageCollectionOfflineDataToApi(
                                CommonUtils.APP_ID,
                                it,
                                CommonUtils.getBatteryStatus(application),
                                CommonUtils.CONTENT_TYPE,
                            )
                        }
                    }
                } else {
                    viewModel.clearAllDataNewUser()
                }
            }
        }
    }

    /**
     *  below three functions are to get users data from room db or server and set it to widgets
     */
    //get user data from room db
    private fun getUserDetailsFromRoom() {
        /**
         * The viewmodel is created lazily upon access, and creation of the viewmodel must be done on the main thread.
         * Simply trying to access the viewmodel on the main thread before accessing it on the lifecycle io scope
         */
        val userDataFlow = userDetailsViewModel.getUserDetailsFromRoom()

        lifecycleScope.launch(Dispatchers.IO) {

            val userData1 = userDataFlow.first()

            if (userData1 == null) {

                getUserDetailsFromApi()
            } else {
                userData = userData1
                userId = userData1.userId
                empType = userData1.employeeType
                userTypeId = userData1.userTypeId

                lifecycleScope.launch(Dispatchers.Main) {
                    setUserDataToWidgets(userData1)
                }
            }
        }

    }

    private fun setUserDataToWidgets(userData: UserData) {


        val fullName = userData.userName
        val maxLength = 20

        if (fullName?.length ?: 0 > maxLength) {
            binding.userFullName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

        } else {

            binding.userFullName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        }

        binding.userFullName.text = fullName


        binding.userEmpId.text = userData.employeeId


        when (userData.employeeType) {
            "N" -> {
                binding.userEmployeeTypeTv.text =
                    resources.getString(R.string.waste_employee)
            }

            "S" -> {
                binding.userEmployeeTypeTv.text =
                    resources.getString(R.string.street_employee)
            }

            "L" -> {
                binding.userEmployeeTypeTv.text =
                    resources.getString(R.string.liquid_employee)
            }

            "D" -> {
                binding.userEmployeeTypeTv.text =
                    resources.getString(R.string.dump_yard_supervisor)
            }
        }

    }

    /// to handle if there is no data in room db get it from server
    private fun getUserDetailsFromApi() {

        lifecycleScope.launch {

            val userEssentials = viewModel.userEssentialsFlow.first()
            userDetailsViewModel.getUserDetails(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                userEssentials.userId,
                userEssentials.userTypeId,
                userEssentials.employeeType
            )

        }

    }

    private fun getUserDetailsUpdateFromApi() {

        lifecycleScope.launch {

            val userEssentials = viewModel.userEssentialsFlow.first()
            userDetailsViewModel.getUserDetailsUpdate(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                userEssentials.userId,
                userEssentials.userTypeId,
                userEssentials.employeeType,
                binding.userFullName.text.trim().toString(),
                "null"
            )

        }

    }

    private fun showAlertDialog(
        titleResId: Int,
        messageResId: Int,
        dialogType: String
    ) {

        alertMessageDialogFrag.setTitleAndMsg(
            resources.getString(titleResId),
            resources.getString(messageResId),
            dialogType
        )

        if (!alertMessageDialogFrag.isAdded)
            alertMessageDialogFrag.show(
                supportFragmentManager,
                AlertMessageDialogFrag.TAG
            )

    }

    private fun showSettingsBottomSheet() {
        if (!settingsBottomSheet.isAdded) {
            settingsBottomSheet.show(supportFragmentManager, SettingBottomSheetFrag.TAG)
            settingsBottomSheet.setIsBifurcationOn(isBifurcationOn)
            settingsBottomSheet.setIsVehicleScanOn(isVehicleScanOn)
            settingsBottomSheet.setEmpType(empType)
            settingsBottomSheet.setListener(this)
        }

    }

    private fun showSettingsTeamBottomSheet() {
        if (!settingsTeamBottomSheet.isAdded) {
            settingsTeamBottomSheet.show(supportFragmentManager, SettingTeamsBottomSheetFrag.TAG)
            settingsTeamBottomSheet.setIsTeamsOn(isTeamSelected)
            settingsTeamBottomSheet.setListener(this)
        }

    }

    private fun showChangeLanguageBottomSheet() {

        if (!languageBottomSheet.isAdded) {
            languageBottomSheet.setListener(this)
            languageBottomSheet.show(
                supportFragmentManager,
                LanguageBottomSheetFrag.TAG
            )
            if (selectedLanguage != null) languageBottomSheet.setPreferredLang(
                selectedLanguage!!
            )

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun expandFab() {

        binding.fabLanguageText.isClickable = true
        binding.fabLogoutText.isClickable = true
        binding.fabSettingText.isClickable = true
        binding.fabPrivacyPolicyText.isClickable = true
        binding.fabSettingTeamText.isClickable = true

        binding.fabLanguage.isClickable = true
        binding.fabLogout.isClickable = true
        binding.fabSetting.isClickable = true
        binding.fabPrivacyPolicy.isClickable = true
        binding.fabSettingTeam.isClickable = true

        lifecycleScope.launch(Dispatchers.IO) {

            val userDataFlow = userDetailsViewModel.getUserDetailsFromRoom()
            val userData1 = userDataFlow.first()
            val employeeType1 = userData1?.employeeType

            withContext(Dispatchers.Main) {
                binding.addFab.startAnimation(
                    fabClockAnim
                )

                binding.fabLanguage.startAnimation(
                    fabOpenAnim
                )
                binding.fabLanguageText.startAnimation(
                    fabOpenAnim
                )
                binding.fabLogout.startAnimation(
                    fabOpenAnim
                )
                binding.fabLogoutText.startAnimation(
                    fabOpenAnim
                )
                if (employeeType1 != "L" && employeeType1 != "S") {
                    binding.fabSetting.startAnimation(
                        fabOpenAnim
                    )
                    binding.fabSettingText.startAnimation(
                        fabOpenAnim
                    )
                    binding.fabSettingTeam.visibility = View.GONE
                    binding.fabSettingTeamText.visibility = View.GONE
                } else {
                    binding.fabSettingTeamText.startAnimation(
                        fabOpenAnim
                    )
                    binding.fabSettingTeam.startAnimation(
                        fabOpenAnim
                    )
                    binding.fabSetting.visibility = View.GONE
                    binding.fabSettingText.visibility = View.GONE
                    binding.fabSettingTeam.visibility = View.VISIBLE
                    binding.fabSettingTeamText.visibility = View.VISIBLE
                }

                binding.fabPrivacyPolicy.startAnimation(
                    fabOpenAnim
                )
                binding.fabPrivacyPolicyText.startAnimation(
                    fabOpenAnim
                )

                binding.transparentWhiteBg.startAnimation(
                    fromBottomToTop
                )
                isAllFabVisible = true
                //  binding.transparentWhiteBg.visibility = View.VISIBLE

                disableClickOnOtherViews()

            }
        }


    }

    private fun shrinkFab() {

        binding.fabLanguageText.isClickable = false
        binding.fabLogoutText.isClickable = false
        binding.fabSettingText.isClickable = false
        binding.fabPrivacyPolicyText.isClickable = false
        binding.fabSettingTeamText.isClickable = false

        binding.fabLanguage.isClickable = false
        binding.fabLogout.isClickable = false
        binding.fabSetting.isClickable = false
        binding.fabPrivacyPolicy.isClickable = false
        binding.fabSettingTeam.isClickable = false

        lifecycleScope.launch(Dispatchers.IO) {

            val userDataFlow = userDetailsViewModel.getUserDetailsFromRoom()
            val userData1 = userDataFlow.first()
            val employeeType1 = userData1?.employeeType

            withContext(Dispatchers.Main) {

                binding.fabLanguage.startAnimation(
                    fabCloseAnim
                )
                binding.fabLanguageText.startAnimation(
                    fabCloseAnim
                )
                binding.fabLogout.startAnimation(
                    fabCloseAnim
                )
                binding.fabLogoutText.startAnimation(
                    fabCloseAnim
                )
                if (employeeType1 != "L" && employeeType1 != "S") {
                    binding.fabSetting.startAnimation(
                        fabCloseAnim
                    )
                    binding.fabSettingText.startAnimation(
                        fabCloseAnim
                    )
                    binding.fabSettingTeamText.visibility = View.GONE
                    binding.fabSettingTeam.visibility = View.GONE
                } else {
                    binding.fabSettingTeamText.startAnimation(
                        fabCloseAnim
                    )
                    binding.fabSettingTeam.startAnimation(
                        fabCloseAnim
                    )
                    binding.fabSetting.visibility = View.GONE
                    binding.fabSettingText.visibility = View.GONE
                }
                binding.fabPrivacyPolicy.startAnimation(
                    fabCloseAnim
                )
                binding.fabPrivacyPolicyText.startAnimation(
                    fabCloseAnim
                )

                binding.addFab.startAnimation(
                    fabAntiClockAnim
                )

                binding.transparentWhiteBg.startAnimation(
                    fromTopToBottom
                )
                //  binding.transparentWhiteBg.visibility = View.GONE
                enableClickOnOtherViews()
                isAllFabVisible = false

            }
        }

    }


    private fun initToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_action_icon)
        binding.toolbar.title = MyApplication.ULB_NAME
        setSupportActionBar(binding.toolbar)
    }

    private fun setUpGridRecyclerView() {

        binding.dashboardRecyclerView.setHasFixedSize(true)
        binding.dashboardRecyclerView.layoutManager = GridLayoutManager(this, 2)

        val mList = ArrayList<DashboardMenu>()
        setUpMenuList(mList)

    }

    private fun setUpMenuList(mList: ArrayList<DashboardMenu>) {

        lifecycleScope.launch(Dispatchers.IO) {

            val userDataFlow = userDetailsViewModel.getUserDetailsFromRoom()
            val userData1 = userDataFlow.first()
            val employeeType1 = userData1?.employeeType

            withContext(Dispatchers.Main) {
                mList.add(
                    DashboardMenu(
                        resources.getString(R.string.title_activity_qrcode_scanner),
                        R.drawable.ic_qr_code
                    )
                )

                if (employeeType1 != "D")
                    mList.add(
                        DashboardMenu(
                            resources.getString(R.string.title_activity_take_photo),
                            R.drawable.ic_photograph
                        )
                    )

                mList.add(
                    DashboardMenu(
                        resources.getString(R.string.title_activity_history_page),
                        R.drawable.ic_history
                    )
                )

                mList.add(
                    DashboardMenu(
                        resources.getString(R.string.title_activity_sync_offline),
                        R.drawable.ic_sync
                    )
                )

                mList.add(
                    DashboardMenu(
                        resources.getString(R.string.title_activity_profile_page),
                        R.drawable.ic_id_card
                    )
                )
                mList.add(
                    DashboardMenu(
                        resources.getString(R.string.title_activity_my_location),
                        R.drawable.live_location
                    )
                )

                dashboardAdapter = DashboardAdapter(mList)
                dashboardAdapter.setListener(this@DashboardActivity)
                binding.dashboardRecyclerView.adapter = dashboardAdapter
            }
        }

    }

    override fun onMenuItemClicked(menuItem: DashboardMenu) {

        when (menuItem.menuName) {

            resources.getString(R.string.title_activity_qrcode_scanner) -> {

                viewModel.onQrMenuClicked(isDutyOn)
            }

            resources.getString(R.string.title_activity_sync_offline) -> {

                viewModel.onSyncMenuClicked()
            }

            resources.getString(R.string.title_activity_history_page) -> {

                viewModel.onWorkHistoryMenuClicked()
            }

            resources.getString(R.string.title_activity_profile_page) -> {

                viewModel.onProfileMenuClicked()
            }

            resources.getString(R.string.title_activity_take_photo) -> {

                viewModel.onTakePhotoMenuClicked(isDutyOn)
            }

            resources.getString(R.string.title_activity_my_location) -> {

                viewModel.onMyLocationMenuClicked(isDutyOn)
            }
        }

    }

    override fun onVehicleDialogSubmitBtnClicked(
        vehicleId: String, vehicleTypeName: String, vehicleNumber: String
    ) {

        userVehicleDetailsTemp =
            UserVehicleDetails(vehicleId, vehicleTypeName, vehicleNumber)

        if (latitude == null && longitude == null) {
            CustomToast.showWarningToast(this, "Couldn't find location")
        } else {
            val inPunchRequest = userId?.let {
                empType?.let { employeeType ->
                    InPunchRequest(
                        DateTimeUtils.getServerTime(),
                        DateTimeUtils.getYyyyMMddDate(),
                        latitude!!,
                        longitude!!,
                        it,
                        vehicleId,
                        vehicleNumber,
                        employeeType
                    )
                }
            }
            if (inPunchRequest != null) {
                viewModel.saveInPunchDetails(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    CommonUtils.getBatteryStatus(application),
                    inPunchRequest,
                    null
                )
            }
        }
    }

    override fun onVehicleDialogItemClicked(
        vehicleId: String,
        vehicleTypeName: String
    ) {
        Log.i("VEHICLE_CHECK", "onDialogItemClicked: $vehicleTypeName")
        viewModel.getVehicleNumberList(
            CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE, vehicleId
        )
    }

    override fun onVehicleDialogDismissed(isDismissWithoutSubmit: Boolean) {

        enableDutyToggle()

        if (isDismissWithoutSubmit) {
            binding.userAttendanceToggle.isChecked = false
        }
    }

    override fun onDumpYardIdSelected(dumpYardId: String) {
        val inPunchRequest = userId?.let {
            empType?.let { employeeType ->
                InPunchRequest(
                    DateTimeUtils.getServerTime(),
                    DateTimeUtils.getYyyyMMddDate(),
                    latitude!!,
                    longitude!!,
                    it,
                    "1",
                    "1",
                    employeeType,
                    dumpYardId
                )
            }
        }
        if (inPunchRequest != null) {

            val userVehicleDetails = UserVehicleDetails("1", "", dumpYardId)
            viewModel.saveInPunchDetails(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                CommonUtils.getBatteryStatus(application),
                inPunchRequest,
                userVehicleDetails
            )
        }
    }

    override fun onSubmitLanguageDialog(appLanguage: AppLanguage) {
        shrinkFab()
        disableClickOnOtherViews()
        viewModel.onLanguageDialogSubmitBtnClicked(appLanguage)
    }

    override fun onSelectTeamsValueChanged(teamsOn: Boolean) {
        showProgressBar()
        viewModel.setTeamSelected(teamsOn)
        settingsTeamBottomSheet.dismiss()
        shrinkFab()
        hideProgressBar()
      /*  CustomToast.showSuccessToast(
            this, resources.getString(R.string.bifurcation_setting_changed)
        )*/

    }

    override fun onBifurcationValueChanged(isBifurcationOn: Boolean) {
        showProgressBar()
        viewModel.saveIsBifurcationOn(isBifurcationOn)
        settingsBottomSheet.dismiss()
        shrinkFab()
        hideProgressBar()
        CustomToast.showSuccessToast(
            this, resources.getString(R.string.bifurcation_setting_changed)
        )
    }

    override fun onVehicleScanValueChanged(isVehicleScanOn: Boolean) {

        showProgressBar()
        viewModel.saveIsVehicleScanOn(isVehicleScanOn)
        settingsBottomSheet.dismiss()
        shrinkFab()
        hideProgressBar()
        CustomToast.showSuccessToast(
            this,
            resources.getString(R.string.vehicle_scan_setting_changed)
        )
    }

    private fun dismissAlertDialog() {
        alertMessageDialogFrag.dismiss()
    }

    override fun onAlertDialogYesBtnClicked(type: String) {
        lifecycleScope.launch {
            val gcCount = garbageCollectionViewModel.getGcCount()
            viewModel.onAlertDialogYesBtnClicked(type, isDutyOn, gcCount)
        }
    }

    override fun onAlertDialogDismiss() {
        enableDutyToggle()
    }

    //get location instantly for duty operation
    private fun getInstantLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val priority = PRIORITY_HIGH_ACCURACY
            lifecycleScope.launch {
                val location =
                    LocationServices.getFusedLocationProviderClient(this@DashboardActivity)
                        .awaitCurrentLocation(priority)
                if (location != null)

                    viewModel.saveUserLocation(
                        UserLatLong(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            ""
                        )
                    )
            }
        }
    }

    //for turning on the gps
    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        if (activityResult.resultCode == RESULT_OK) {
            binding.gpsProgressLayout.visibility = View.VISIBLE

            lifecycleScope.launch {

                delay(900)

                if (isGpsOn) {

                    binding.gpsProgressLayout.visibility = View.GONE
                    viewModel.onDutyToggleClicked(
                        isInternetOn,
                        isGpsOn,
                        isDutyToggleChecked,
                        isVehicleScanOn,
                        userId,
                        CommonUtils.getBatteryStatus(application)
                    )

                }

            }

        } else if (activityResult.resultCode == RESULT_CANCELED) {
            // The user was asked to change settings, but chose not to
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
            enableDutyToggle()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun enableClickOnOtherViews() {

        lifecycleScope.launch {

            delay(200)

            enableDutyToggle()
            dashboardAdapter.setClickable(true)
            dashboardAdapter.notifyDataSetChanged()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun disableClickOnOtherViews() {

        disableDutyToggle()
        dashboardAdapter.setClickable(false)
        dashboardAdapter.notifyDataSetChanged()

    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isAllFabVisible) {
                val outRect = Rect()
                binding.fabConstraint.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    shrinkFab()
                    enableClickOnOtherViews()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    //lazy initializing fab animations
    private val fabOpenAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this, R.anim.fab_open
        )
    }
    private val fabCloseAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this, R.anim.fab_close
        )
    }
    private val fabClockAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this, R.anim.fab_rotate_clock
        )
    }
    private val fabAntiClockAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this, R.anim.fab_rotate_anti_clock
        )
    }

    private val slideInUpAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
    }
    private val slideOutUpAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.slide_out_up)
    }

    private val fromBottomToTop: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim)
    }
    private val fromTopToBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }


    private fun setUpFabBtn() {

        binding.addFab.setOnClickListener {

            if (!isAllFabVisible) {
                expandFab()
            } else {
                shrinkFab()
            }
        }

        binding.fabLogoutText.setOnClickListener {
            viewModel.onLogOutFabMenuClicked()
        }
        binding.fabLogout.setOnClickListener {
            viewModel.onLogOutFabMenuClicked()
        }
        binding.fabLanguage.setOnClickListener {
            viewModel.onChangeLanguageFabMenuClicked()
        }
        binding.fabLanguageText.setOnClickListener {
            viewModel.onChangeLanguageFabMenuClicked()
        }
        binding.fabSetting.setOnClickListener {
            viewModel.onSettingFabMenuClicked()
        }
        binding.fabSettingText.setOnClickListener {
            viewModel.onSettingFabMenuClicked()
        }
        binding.fabPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicyFabMenuClicked()
        }
        binding.fabPrivacyPolicyText.setOnClickListener {
            viewModel.onPrivacyPolicyFabMenuClicked()
        }
        binding.fabSettingTeamText.setOnClickListener {
            viewModel.onSettingTeamFabMenuClicked()
        }
        binding.fabSettingTeam.setOnClickListener {
            viewModel.onSettingTeamFabMenuClicked()
        }

    }

    private fun handleBackBtnInDashboard() {
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (isAllFabVisible) {
                    shrinkFab()
                    enableClickOnOtherViews()

                } else BackBtnPressedUtil.exitOnBackPressed(
                    this
                )
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isAllFabVisible) {
                            shrinkFab()
                            enableClickOnOtherViews()
                        } else BackBtnPressedUtil.exitOnBackPressed(this@DashboardActivity)
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(serviceReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun startNetworkSpeedMonitor(onSpeedUpdate: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var previousRxBytes = TrafficStats.getTotalRxBytes()
            var previousTxBytes = TrafficStats.getTotalTxBytes()

            while (true) {
                delay(1000) // every second
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()

                val downloadSpeed =
                    (currentRxBytes - previousRxBytes) * 8 / 1024 // Kbps
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