package com.appynitty.kotlinsbalibrary.common.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.ui.select_ulb_module.SelectUlb
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.empDashboard.EmpDashboardActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    companion object {
        private const val UPDATE_REQUEST_CODE = 1001
    }
    private val viewModel: SplashViewModel by viewModels()
    private lateinit var versionCodeTv: TextView
    private var versionCode: Int = 0

    //setting app language ( by default it will be marathi if user doesn't change language )
    override fun attachBaseContext(newBase: Context?) {
        var context: Context? = newBase
        if (newBase != null) {
            val languageDataStore = LanguageDataStore(newBase.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            context = newBase.let { LanguageConfig.changeLanguage(it, appLanguage.languageId) }
        }
        super.attachBaseContext(context)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkForImmediateUpdate()
        viewModel.checkWhereToNavigate()
        // listening to events sent by viewModel
        lifecycleScope.launchWhenStarted {
            viewModel.splashEventsFlow.collect { event ->

                when (event) {
                    SplashViewModel.SplashEvent.NavigateToDashboardScreen -> {
                        navigateToDashboardScreen()
                    }

                    SplashViewModel.SplashEvent.NavigateToEmpDashBoardScreen -> {
                        navigateToEmpDashboardScreen()
                    }

                    SplashViewModel.SplashEvent.NavigateToLoginScreen -> {
                        navigateToSelectUlbScreen()
                    }

                    is SplashViewModel.SplashEvent.ShowErrorMsg -> {
                        CustomToast.showErrorToast(this@SplashActivity, event.msg)
                    }
                }
            }
        }
    }

    private fun navigateToEmpDashboardScreen() {
        val intent = Intent(this, EmpDashboardActivity::class.java)
        startAnotherActivity(intent)
    }

    private fun navigateToDashboardScreen() {
        val intent = Intent(this, DashboardActivity::class.java)
        startAnotherActivity(intent)
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startAnotherActivity(intent)
    }

    private fun navigateToSelectUlbScreen() {
        val intent = Intent(this, SelectUlb::class.java)
        startAnotherActivity(intent)
    }

    private fun startAnotherActivity(intent: Intent) {
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
            finish()
        }, 2000)

    }

    private fun checkForImmediateUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true)
                    .build()

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    this, // Activity
                    appUpdateOptions,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume update if already in progress
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true)
                    .build()

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    this,
                    appUpdateOptions,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }
}