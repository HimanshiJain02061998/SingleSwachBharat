package com.appynitty.kotlinsbalibrary.housescanify.ui.empDashboard

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.graphics.Rect
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.location.GisLocationService
import com.appynitty.kotlinsbalibrary.common.location.awaitCurrentLocation
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.ui.my_location.MyLocationActivity
import com.appynitty.kotlinsbalibrary.common.ui.privacyPolicy.PrivacyPolicyActivity
import com.appynitty.kotlinsbalibrary.common.ui.userDetails.viewmodel.UserDetailsViewModel
import com.appynitty.kotlinsbalibrary.common.utils.*
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.UserLatLong
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.AlertMessageDialogFrag
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.LanguageBottomSheetFrag
import com.appynitty.kotlinsbalibrary.common.utils.permission.LocationPermission
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.EmpActivityDashboardBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardAdapter
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardMenu
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardViewModel
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchInRequest
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpPunchOutRequest
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpGcRepository
import com.appynitty.kotlinsbalibrary.housescanify.ui.empHistory.EmpHistoryActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.empQrScanner.EmpQrScannerActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline.EmpGcViewModelFactory
import com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline.EmpSyncGcViewModel
import com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline.EmpSyncOfflineActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val TAG = "EmpDashboardActivity"

@AndroidEntryPoint
class EmpDashboardActivity : AppCompatActivity(), DashboardAdapter.MenuItemClickedInterface,
    LanguageBottomSheetFrag.LanguageDialogCallbacks,
    AlertMessageDialogFrag.AlertMessageDialogCallBacks {

    private val viewModel: EmpDashboardViewModel by viewModels()
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()

    //garbage viewModel has an application scope as it is used for syncing functionality
    //it is required in two activities dashboard and sync offline
    @Inject
    lateinit var empGcDao: EmpGcDao

    @Inject
    lateinit var houseOnMapDao: EmpHouseOnMapDao

    @Inject
    lateinit var empHouseOnMapDao: EmpHouseOnMapDao

    @Inject
    lateinit var archivedDao: ArchivedDao

    @Inject
    lateinit var empGcRepository: EmpGcRepository
    private lateinit var empSyncGcViewModel: EmpSyncGcViewModel

    private lateinit var binding: EmpActivityDashboardBinding
    private lateinit var mContext: Context
    private var isDutyOn = false
    private var isUserLogin = false
    private var userData: UserData? = null
    private var userId: String? = null
    private var empType: String? = null
    private var userTypeId: String? = null
    private var isAllFabVisible = false
    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var languageBottomSheet: LanguageBottomSheetFrag
    private lateinit var languageDataStore: LanguageDataStore
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private var isDutyToggleChecked = false
    private var isInternetOn = true
    private var latitude: String? = null
    private var longitude: String? = null
    private var isGpsOn = false
    private lateinit var locationPermission: LocationPermission
    private lateinit var syncSnackBar: Snackbar
    private var isSyncingOn = false
    private lateinit var alertMessageDialogFrag: AlertMessageDialogFrag
    private var isDutyOnToggleClicked = false

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = EmpActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        initVars()
        getUserDetailsFromRoom()
        subscribeLiveData()
        subscribeChannelEvents()
        setUpFabBtn()
        setUpGridRecyclerView()
        registerClickedEvents()
        handleBackBtnInDashboard()
        viewModel.getAllPropertyTypesFromApi(CommonUtils.APP_ID)
    }

    override fun onResume() {
        super.onResume()
        isDutyOnToggleClicked = false
        viewModel.checkIsDateChanged()
    }

    private fun registerClickedEvents() {

        binding.userAttendanceToggle.setOnClickListener {

            disableDutyToggle()
            isDutyToggleChecked = binding.userAttendanceToggle.isChecked
            binding.userAttendanceToggle.isChecked = isDutyOn
            getInstantLocation()

            viewModel.onDutyToggleClicked(isInternetOn, isGpsOn, isDutyToggleChecked)
        }

        binding.userAttendanceToggle.setOnTouchListener(View.OnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        })
    }

    private fun getInstantLocation() {

        val priority = Priority.PRIORITY_HIGH_ACCURACY
        lifecycleScope.launch {
            val location =
                LocationServices.getFusedLocationProviderClient(this@EmpDashboardActivity)
                    .awaitCurrentLocation(priority)
            if (location != null) viewModel.saveUserLocation(
                UserLatLong(
                    location.latitude.toString(), location.longitude.toString(), ""
                )
            )
        }

    }

    private fun saveInPunchDetails() {
        if (latitude == null && longitude == null) {
            CustomToast.showWarningToast(this, "Couldn't find location")
        } else {

            val inPunchRequest = userId?.let {
                EmpPunchInRequest(
                    it,
                    DateTimeUtils.getServerTime(),
                    DateTimeUtils.getYyyyMMddDate(),
                    latitude!!,
                    longitude!!,
                )
            }
            if (inPunchRequest != null) {
                viewModel.saveEmpPunchInDetails(
                    CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE, inPunchRequest
                )
            }

        }
    }

    private fun initToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_action_icon)
        binding.toolbar.title = resources.getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)
    }

    private fun initVars() {

        locationPermission = LocationPermission(this)
        locationPermission.initFineLocationPermission(this)

        alertMessageDialogFrag = AlertMessageDialogFrag()
        alertMessageDialogFrag.setListener(this)
        mContext = this@EmpDashboardActivity
        languageBottomSheet = LanguageBottomSheetFrag()


        val empGcViewModelFactory = EmpGcViewModelFactory(
            application, empGcDao, empGcRepository, archivedDao, houseOnMapDao
        )
        empSyncGcViewModel = ViewModelProvider(
            MyApplication.instance, empGcViewModelFactory
        )[EmpSyncGcViewModel::class.java]

        syncSnackBar = Snackbar.make(
            binding.nestedCoordinatorLayout,
            resources.getString(R.string.syncing_is_on),
            Snackbar.LENGTH_INDEFINITE
        )
        syncSnackBar.view.background =
            ResourcesCompat.getDrawable(resources, R.drawable.snackbar_bg, null)
    }

    private fun getUserDetailsFromRoom() {

        lifecycleScope.launch {
            val userData1 = userDetailsViewModel.getUserDetailsFromRoom().first()
            if (userData1 == null) {
                //if no data inside room fetching it from api
                getUserDetailsFromApi()
            } else {

                userData = userData1
                userId = userData1.userId
                empType = userData1.employeeType
                userTypeId = userData1.userTypeId
                setUserDataToWidgets(userData1)
            }
        }
    }

    private fun setUserDataToWidgets(userData: UserData) {

        val fullName = userData.userName
        val maxLength = 20

        if (fullName?.length?:0 > maxLength) {
            binding.userFullName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

        } else {

            binding.userFullName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
       }

        binding.userFullName.text = fullName


        try {

            binding.userEmpCodeValue.text = userData.employeeId
            binding.userPartnerNameValue.text = userData.userpartnerName
            binding.userPartenerCodeValue.text = userData.userpartnerCode
        }catch (e:Exception){

        }
    }

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
                binding.userPartnerNameValue.text.trim().toString()

            )

        }

    }

    private fun submitOfflineData() {
        if (!isSyncingOn) {
            lifecycleScope.launch {
                val gcCount = empSyncGcViewModel.getGcCount()
                if (gcCount > 0) {
                    empSyncGcViewModel.saveGarbageCollectionOfflineDataToApi(
                        CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE
                    )
                }
            }
        }
    }

    private fun subscribeChannelEvents() {
        lifecycleScope.launch {

            viewModel.empDashboardEventsFlow.collect { event ->

                when (event) {
                    is EmpDashboardViewModel.EmpDashboardEvent.TurnDutyOn -> {
                        isDutyOnToggleClicked = true
                        saveInPunchDetails()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.EnableDutyToggle -> {
                        enableDutyToggle()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.NavigateToEmpQrScreen -> {
                        navigateEmpQrScreen()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.NavigateToEmpSyncOfflineScreen -> {
                        navigateEmpSyncOfflineScreen()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.NavigateToEmpWorkHistoryScreen -> {
                        navigateEmpWorkHistoryScreen()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowAlertDialog -> {
                        showAlertDialog(
                            event.titleResourceId, event.messageResourceId, event.dialogType
                        )
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowWarningMessage -> {
                        CustomToast.showWarningToast(
                            this@EmpDashboardActivity, resources.getString(event.resourceId)
                        )
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.TurnDutyOff -> {
                        turnDutyOff()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.TurnGpsOn -> {
                        TurnOnGps.gpsStatusCheck(this@EmpDashboardActivity, resolutionForResult)
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.DismissAlertDialogFrag -> {
                        dismissAlertMessageDialogFrag()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.NavigateToLoginScreen -> {
                        navigateToLoginScreen()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowChangeLanguageScreen -> {
                        showChangeLanguageBottomSheet()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.NavigateToPrivacyPolicyScreen -> {
                        navigateToPrivacyPolicyScreen()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.DismissLanguageDialog -> {
                        languageBottomSheet.dismiss()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.RestartDashboardActivity -> {
                        restartActivity()
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.HideProgressBar -> {
                        hideProgressBar()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@EmpDashboardActivity, event.msg)
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.ShowProgressBar -> {
                        showProgressBar()
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowResponseErrorMessage -> {
                        showApiErrorMessage(event.msg, event.msgMr)
                    }

                    is EmpDashboardViewModel.EmpDashboardEvent.ShowResponseSuccessMessage -> {
                        showApiSuccessMessage(event.msg, event.msgMr)
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.StartLocationTracking -> {
                        startLocationTracking()
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.StopLocationTracking -> {
                        stopLocationTracking()
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.CheckIfServiceIsRunning -> {
                        viewModel.shouldStartLocationService(isMyServiceRunning(GisLocationService::class.java))
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.HitGisServer -> {

                    }
                    EmpDashboardViewModel.EmpDashboardEvent.NavigateToMyLocationScreen -> {
                        val mapsIntent =
                            Intent(this@EmpDashboardActivity, MyLocationActivity::class.java)
                        if (latitude != null && latitude?.isNotEmpty() == true){
                            Log.i("latttittttttt", "subscribeChannelEvents: $latitude")
                            mapsIntent.putExtra("latitude", latitude?.toDouble())
                            mapsIntent.putExtra("longitude", longitude?.toDouble())
                        }

                        startActivity(mapsIntent)
                    }

                    EmpDashboardViewModel.EmpDashboardEvent.UserDetailsUpdate -> {

                        getUserDetailsUpdateFromApi()

                    }
                }
            }
        }
    }

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


    private fun enableDutyToggle() {
        binding.userAttendanceToggle.isEnabled = true
    }

    private fun disableDutyToggle() {
        binding.userAttendanceToggle.isEnabled = false
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

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm =
            this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = this.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)

    }

    private fun batteryOptimizationRequest() {

        val intent = Intent()
        val packageName = this.applicationContext.packageName
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:${getPackageName()}")
            this.startActivity(intent)
        }

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


    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun restartActivity() {
        binding.progressBar.visibility = View.VISIBLE

        val intent = intent

        lifecycleScope.launch {
            delay(200)

            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
        }


    }

    private fun startAnotherActivity(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )
    }

    private fun navigateToLoginScreen() {
        userDetailsViewModel.deleteAllUserDataFromRoom()
        val intent = Intent(this, LoginActivity::class.java)
        startAnotherActivity(intent)
        finish()
    }

    private fun navigateToPrivacyPolicyScreen() {

        showProgressBar()
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startAnotherActivity(intent)

        lifecycleScope.launch {
            delay(50)
            hideProgressBar()
            shrinkFab()
        }
    }

    private fun navigateEmpQrScreen() {

        val intent = Intent(this, EmpQrScannerActivity::class.java)
        intent.putExtra("empType", empType)
        intent.putExtra("userId", userId)
        intent.putExtra("userTypeId", userTypeId)
        intent.putExtra("languageId", selectedLanguageId)
        startAnotherActivity(intent)
    }

    private fun navigateEmpSyncOfflineScreen() {
        val intent = Intent(this, EmpSyncOfflineActivity::class.java)
        intent.putExtra("languageId", selectedLanguageId)
        startAnotherActivity(intent)
    }

    private fun navigateEmpWorkHistoryScreen() {
        val intent = Intent(this, EmpHistoryActivity::class.java)
        intent.putExtra("userId", userId)
        startAnotherActivity(intent)
    }

    private fun showChangeLanguageBottomSheet() {
        if (!languageBottomSheet.isAdded) {
            languageBottomSheet.setListener(this)
            languageBottomSheet.show(supportFragmentManager, LanguageBottomSheetFrag.TAG)
            if (selectedLanguage != null) languageBottomSheet.setPreferredLang(selectedLanguage!!)
        }
    }

    private fun showAlertDialog(titleResId: Int, messageResId: Int, dialogType: String) {

        alertMessageDialogFrag.setTitleAndMsg(
            resources.getString(titleResId), resources.getString(messageResId), dialogType
        )
        if (!alertMessageDialogFrag.isAdded) alertMessageDialogFrag.show(
            supportFragmentManager, AlertMessageDialogFrag.TAG
        )

    }

    private fun subscribeLiveData() {

        viewModel.userLocationLiveData.observe(this) {
            latitude = it.latitude
            longitude = it.longitude
        }

        GpsStatusListener(this).observe(this) {
            isGpsOn = it
        }
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
            if (isInternetOn) {
                submitOfflineData()
            }
        }
        viewModel.isUserLoggedInLiveData.observe(this) {
            isUserLogin = it
        }

        viewModel.isUserDutyOnFlow.asLiveData().observe(this) {
            isDutyOn = it
            binding.userAttendanceToggle.isChecked = it
            enableDutyToggle()

            if (isDutyOn) {
                binding.userAttendanceStatus.setTextColor(
                    resources.getColor(
                        R.color.colorONDutyGreen, null
                    )
                )
                binding.userAttendanceStatus.text = resources.getString(R.string.status_on_duty)

            } else {
                binding.userAttendanceStatus.setTextColor(
                    resources.getColor(
                        R.color.colorOFFDutyRed, null
                    )
                )
                binding.userAttendanceStatus.text = resources.getString(R.string.status_off_duty)
            }
        }


        userDetailsViewModel.userDetailsLiveData.observe(this) {

            when (it) {

                is ApiResponseListener.Success -> {
                    binding.progressBar.visibility = View.GONE

                    getUserDetailsFromRoom()

                }

                is ApiResponseListener.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is ApiResponseListener.Failure -> {
                    binding.progressBar.visibility = View.GONE
                }

            }
        }

        empSyncGcViewModel.isSyncingOnLiveData.observe(this) {

            isSyncingOn = it
            if (it) {
                syncSnackBar.show()

            } else {
                syncSnackBar.dismiss()
            }
        }

        empSyncGcViewModel.empGcOfflineResponseLiveData.observe(this) {

            when (it) {

                is ApiResponseListener.Loading -> {

                }

                is ApiResponseListener.Success -> {

                    CustomToast.showSuccessToast(this, "Batch Synced Successfully")
                }

                is ApiResponseListener.Failure -> {

                    CustomToast.showErrorToast(this, it.message.toString())
                }

                null -> {
                    //no need to take care
                }
            }
        }


    }

    private fun setUpGridRecyclerView() {

        binding.dashboardRecyclerView.setHasFixedSize(true)
        binding.dashboardRecyclerView.layoutManager = GridLayoutManager(this, 2)

        val mList = ArrayList<DashboardMenu>()
        setUpMenuList(mList)

        dashboardAdapter = DashboardAdapter(mList)
        dashboardAdapter.setListener(this)
        binding.dashboardRecyclerView.adapter = dashboardAdapter

    }

    private fun setUpMenuList(mList: ArrayList<DashboardMenu>) {
        mList.add(
            DashboardMenu(
                resources.getString(R.string.title_activity_qrcode_scanner), R.drawable.ic_qr_code
            )
        )
        mList.add(
            DashboardMenu(
                resources.getString(R.string.title_activity_history_page), R.drawable.ic_history
            )
        )
        mList.add(
            DashboardMenu(
                resources.getString(R.string.title_activity_sync_offline), R.drawable.ic_sync
            )
        )
        mList.add(
            DashboardMenu(
                resources.getString(R.string.title_activity_my_location),
                R.drawable.live_location
            )
        )
    }

    override fun onMenuItemClicked(menuItem: DashboardMenu) {

        when (menuItem.menuName) {
            resources.getString(R.string.title_activity_qrcode_scanner) -> {
                viewModel.onQrMenuClicked(isDutyOn)
            }

            resources.getString(R.string.title_activity_sync_offline) -> {
                viewModel.onSyncOfflineMenuClicked()
            }

            resources.getString(R.string.title_activity_history_page) -> {
                viewModel.onWorkHistoryMenuClicked()
            }


            resources.getString(R.string.title_activity_my_location) -> {

                viewModel.onMyLocationMenuClicked(isDutyOn)
            }
        }
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
        binding.fabPrivacyPolicy.setOnClickListener {
            viewModel.onPrivacyPolicyFabMenuClicked()
        }
        binding.fabPrivacyPolicyText.setOnClickListener {
            viewModel.onPrivacyPolicyFabMenuClicked()
        }
    }

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

    @SuppressLint("NotifyDataSetChanged")
    private fun expandFab() {

        binding.fabLanguageText.isClickable = true
        binding.fabLogoutText.isClickable = true
        binding.fabPrivacyPolicyText.isClickable = true

        binding.fabLanguage.isClickable = true
        binding.fabLogout.isClickable = true
        binding.fabPrivacyPolicy.isClickable = true

        binding.addFab.startAnimation(
            fabClockAnim
        )
        binding.fabPrivacyPolicy.startAnimation(
            fabOpenAnim
        )
        binding.fabPrivacyPolicyText.startAnimation(
            fabOpenAnim
        )
        binding.fabLogout.startAnimation(
            fabOpenAnim
        )
        binding.fabLogoutText.startAnimation(
            fabOpenAnim
        )
        binding.fabLanguage.startAnimation(
            fabOpenAnim
        )
        binding.fabLanguageText.startAnimation(
            fabOpenAnim
        )

        isAllFabVisible = true

        binding.transparentWhiteBg.visibility = View.VISIBLE

        disableClickOnOtherViews()

    }

    private fun shrinkFab() {

        binding.fabLanguageText.isClickable = false
        binding.fabLogoutText.isClickable = false
        binding.fabPrivacyPolicyText.isClickable = false

        binding.fabLanguage.isClickable = false
        binding.fabLogout.isClickable = false
        binding.fabPrivacyPolicy.isClickable = false

        binding.fabLanguage.startAnimation(
            fabCloseAnim
        )
        binding.fabLogout.startAnimation(
            fabCloseAnim

        )
        binding.fabPrivacyPolicy.startAnimation(
            fabCloseAnim

        )
        binding.fabLanguageText.startAnimation(
            fabCloseAnim
        )
        binding.fabLogoutText.startAnimation(
            fabCloseAnim

        )
        binding.fabPrivacyPolicyText.startAnimation(
            fabCloseAnim

        )
        binding.addFab.startAnimation(
            fabAntiClockAnim
        )


        binding.transparentWhiteBg.visibility = View.GONE
        enableClickOnOtherViews()
        isAllFabVisible = false

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

    override fun onSubmitLanguageDialog(appLanguage: AppLanguage) {

        shrinkFab()
        disableClickOnOtherViews()
        viewModel.onLanguageDialogSubmitBtnClicked(appLanguage)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun disableClickOnOtherViews() {

        disableDutyToggle()
        dashboardAdapter.setClickable(false)
        dashboardAdapter.notifyDataSetChanged()

    }

    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        if (activityResult.resultCode == RESULT_OK) {
            binding.gpsProgressLayout.visibility = View.VISIBLE

            lifecycleScope.launch {

                delay(900)

                if (isGpsOn) {
                    binding.gpsProgressLayout.visibility = View.GONE
                    viewModel.onDutyToggleClicked(isInternetOn, isGpsOn, isDutyToggleChecked)
                }
            }


        } else if (activityResult.resultCode == RESULT_CANCELED) {
            // The user was asked to change settings, but chose not to
            enableDutyToggle()
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onAlertDialogYesBtnClicked(type: String) {
        lifecycleScope.launch {
            val gcCount = empSyncGcViewModel.getGcCount()
            viewModel.onAlertDialogYesBtnClicked(type, isDutyOn, gcCount)
        }
    }

    private fun turnDutyOff() {
        if (latitude == null && longitude == null) {
            CustomToast.showWarningToast(this, "Couldn't find location")
        } else {
            val outPunchRequest = userId?.let {
                EmpPunchOutRequest(
                    it,
                    DateTimeUtils.getServerTime(),
                    DateTimeUtils.getYyyyMMddDate(),
                    latitude!!,
                    longitude!!,
                )
            }
            if (outPunchRequest != null) {
                viewModel.saveOutPunchDetails(
                    CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE, outPunchRequest
                )
            }
        }
    }

    private fun dismissAlertMessageDialogFrag() {
        alertMessageDialogFrag.dismiss()
    }

    override fun onAlertDialogDismiss() {
        enableDutyToggle()
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
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isAllFabVisible) {
                        shrinkFab()
                        enableClickOnOtherViews()
                    } else BackBtnPressedUtil.exitOnBackPressed(this@EmpDashboardActivity)
                }
            })
        }
    }
}