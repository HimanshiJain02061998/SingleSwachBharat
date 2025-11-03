package com.appynitty.kotlinsbalibrary.common.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.model.request.LoginRequest
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.AddUlbActivity
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.LanguageBottomSheetFrag
import com.appynitty.kotlinsbalibrary.databinding.ActivityLoginBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.empDashboard.EmpDashboardActivity
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/** CREATED BY SANATH GOSAVI ON 31-10-2022 */
private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), LanguageBottomSheetFrag.LanguageDialogCallbacks {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var binding: ActivityLoginBinding
    private var loginRequest: LoginRequest? = null
    private lateinit var languageDataStore: LanguageDataStore
    private lateinit var languageBottomSheet: LanguageBottomSheetFrag
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private var isInternetOn = false
    private lateinit var userDataStore: UserDataStore

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) {
                    //  showPermissionRequestDialog(it.key)
                }
            }
        }

    private fun initPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        } else {
            //we don't need activity recognition permission below api level 29
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA,
                )
            )
        }

    }

    private fun showPermissionRequestDialog(permission: String) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission
            )
        ) {
            AlertDialog.Builder(this).setTitle(permission)
                .setMessage("permission required").setPositiveButton(
                    "OK"
                ) { _, _ ->
                    requestMultiplePermissions.launch(arrayOf(permission))
                }.create().show()
        } else {
            requestMultiplePermissions.launch(arrayOf(permission))
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userDataStore = UserDataStore(this)
        initPermissions()
        initVars()
        clickEvents()
        subscribeLiveData()
        subscribeChannelEvent()
        onBack()
    }


    private fun onBack() {
        binding.btnBack?.setOnClickListener {

            val intent = Intent(this, AddUlbActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        lifecycleScope.launch {
            val userDataStore = UserDataStore(this@LoginActivity)

            MyApplication.APP_ID = ""
            MyApplication.ULB_NAME = ""
            userDataStore.clearAppId()

            val intent = Intent(this@LoginActivity, AddUlbActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun subscribeChannelEvent() {

        lifecycleScope.launch {
            viewModel.loginEventsFlow.collect { event ->
                when (event) {
                    LoginViewModel.LoginEvent.HideProgressBar -> hideProgressBar()
                    LoginViewModel.LoginEvent.NavigateToDashboard -> {
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        val appId = intent.getStringExtra(userDataStore.getAppId.toString())

                        if (!appId.isNullOrEmpty()) {
                            CustomToast.showSuccessToast(
                                this@LoginActivity,
                                "AppId received: $appId"
                            )
                        }
                        startAnotherActivity(intent)
                    }

                    LoginViewModel.LoginEvent.NavigateToEmpDashboard -> {
                        val intent = Intent(this@LoginActivity, EmpDashboardActivity::class.java)
                        val appId = intent.getStringExtra(userDataStore.getAppId.toString())

                        if (!appId.isNullOrEmpty()) {
                            CustomToast.showSuccessToast(
                                this@LoginActivity,
                                "AppId received: $appId"
                            )
                        }
                        startAnotherActivity(intent)
                    }

                    is LoginViewModel.LoginEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@LoginActivity, event.msg)
                    }

                    LoginViewModel.LoginEvent.ShowProgressBar -> showProgressBar()
                    is LoginViewModel.LoginEvent.ShowResponseErrorMessage -> showApiErrorMessage(
                        event.msg,
                        event.msgMr
                    )

                    is LoginViewModel.LoginEvent.ShowResponseSuccessMessage -> showApiSuccessMessage(
                        event.msg,
                        event.msgMr
                    )

                    LoginViewModel.LoginEvent.DisableLoginButton -> disableLoginButton()
                    LoginViewModel.LoginEvent.EnableLoginButton -> enableLoginButton()
                }
            }
        }

    }

    private fun enableLoginButton() {
        binding.btnLogin.isEnabled = true
    }

    private fun disableLoginButton() {
        binding.btnLogin.isEnabled = false
    }

    private fun startAnotherActivity(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )
        finish()
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
        binding.loginProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.loginProgressBar.visibility = View.GONE
    }

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

    //function to initialize variables
    private fun initVars() {
        languageBottomSheet = LanguageBottomSheetFrag()
        //setting emp type auto complete text view ( exposed drop down menu )
        val empTypesArray = resources.getStringArray(R.array.employee_types)
        val arrayAdapter = ArrayAdapter(this, R.layout.drop_down_emp_type, empTypesArray)
        binding.etEmpType.setAdapter(arrayAdapter)
        lifecycleScope.launch {
            viewModel.getAppAssets(userDataStore.getAppId.first())
        }
    }

    private fun subscribeLiveData() {
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
        }

        viewModel.iconUrl.observe(this, Observer {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.app_icon_white) // optional
                .into(binding.imgLogo)
        })

    }

    //function to register click events
    private fun clickEvents() {

        //click listener on login btn
        binding.btnLogin.setOnClickListener {

            if (isInternetOn) {

                //getting data from widgets
                val userName = binding.etUserName.text.toString().replace("\\s", "").trim()
                val password = binding.etPassword.text.toString().replace("\\s", "").trim()
                val employeeType = binding.etEmpType.text.toString()

                // validating username and password
                if (employeeType.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty()) {

                    // then only proceed
                    logUserIn(userName, password, employeeType)

                } else {

                    if (employeeType.isEmpty() && userName.isEmpty() && password.isEmpty()) {
                        CustomToast.showWarningToast(
                            this,
                            resources.getString(R.string.empty_fields_not_allowed)
                        )
                    } else {
                        if (userName.isEmpty() || userName.isBlank()) {
                            CustomToast.showWarningToast(
                                this,
                                resources.getString(R.string.please_type_username)
                            )
                        }
                        if (password.isEmpty() || password.isBlank()) {
                            CustomToast.showWarningToast(
                                this,
                                resources.getString(R.string.please_type_password)
                            )
                        }
                        if (employeeType.isEmpty() || employeeType.isBlank()) {
                            CustomToast.showWarningToast(
                                this,
                                resources.getString(R.string.please_select_employee_type)
                            )

                        }
                    }
                }
            } else {
                CustomToast.showWarningToast(
                    this,
                    resources.getString(R.string.no_internet_error)
                )
            }
        }

        binding.btnChangeLang.setOnClickListener {

            if (!languageBottomSheet.isAdded) {

                languageBottomSheet.setListener(this)
                languageBottomSheet.show(supportFragmentManager, LanguageBottomSheetFrag.TAG)
                if (selectedLanguage != null)
                    languageBottomSheet.setPreferredLang(selectedLanguage!!)
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun logUserIn(userName: String, password: String, employeeType: String) {

        var empType = ""

        when (employeeType) {
            resources.getString(R.string.household_collection) -> {
                empType = "N"
            }

            resources.getString(R.string.street_sweeping) -> {
                empType = "S"
            }

            resources.getString(R.string.liquid_waste_cleaning) -> {
                empType = "L"
            }

            resources.getString(R.string.dump_yard_supervisor) -> {
                empType = "D"
            }
        }
        // getting device id from android device ( kinda imei )
//        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//        var deviceId: String? = CommonUtils.getAndroidId(this)
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            deviceId = telephonyManager.deviceId
//        }
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager

        var deviceId: String? = try {
            CommonUtils.getAndroidId(this@LoginActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ANDROID_ID", e)
            null
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val telephonyId = telephonyManager?.deviceId
                if (!telephonyId.isNullOrEmpty()) {
                    deviceId = telephonyId
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "READ_PHONE_STATE permission not granted", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting device ID from TelephonyManager", e)
            }
        }
        if (deviceId == null) {
            CustomToast.showErrorToast(this, "Couldn't get device id")
        } else {
            Log.i(TAG, "logUserIn: $empType")
            loginRequest = LoginRequest(empType, password, userName, deviceId)

            lifecycleScope.launch {
                val appId = userDataStore.getAppId.first()
                viewModel.saveLoginDetails(
                    appId,
                    CommonUtils.CONTENT_TYPE,
                    loginRequest!!
                )
            }

        }

    }

    override fun onSubmitLanguageDialog(appLanguage: AppLanguage) {
        lifecycleScope.launch(Dispatchers.IO) {
            languageDataStore.savePreferredLanguage(appLanguage)
        }
        binding.loginProgressBar.visibility = View.VISIBLE

        val intent = intent
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)

        Handler(Looper.myLooper()!!).postDelayed({
            binding.loginProgressBar.visibility = View.GONE
        }, 300)
        languageBottomSheet.dismiss()
    }

}