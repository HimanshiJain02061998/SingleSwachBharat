package com.appynitty.kotlinsbalibrary.common.ui.splash

import android.annotation.SuppressLint
import android.app.Activity
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
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.AddUlbActivity
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
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
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var versionCodeTv: TextView

    companion object {
        private const val UPDATE_REQUEST_CODE = 1001
        private const val SPLASH_DELAY = 2000L
    }

    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    // Language setup
    override fun attachBaseContext(newBase: Context?) {
        var context = newBase
        newBase?.let {
            val languageDataStore = LanguageDataStore(it.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            context = LanguageConfig.changeLanguage(it, appLanguage.languageId)
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupVersionText()
        observeSplashEvents()
    }

    private fun setupVersionText() {
        versionCodeTv = findViewById(R.id.versionCodeTv)
        versionCodeTv.text = "Version : ${CommonUtils.VERSION_CODE}"
    }

    // In-app update check
    private fun checkForImmediateUpdate() {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    ) {
                        val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                            .setAllowAssetPackDeletion(true)
                            .build()

                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            this,
                            options,
                            UPDATE_REQUEST_CODE
                        )
                    } else {
                        // No update → navigate
                        viewModel.checkWhereToNavigate()
                    }
                }
                .addOnFailureListener {
                    viewModel.checkWhereToNavigate()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.checkWhereToNavigate()
        }
    }

    // Events observer (UNCHANGED events)
    private fun observeSplashEvents() {
        lifecycleScope.launch {
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
                        CustomToast.showErrorToast(
                            this@SplashActivity,
                            event.msg
                        )
                    }
                }
            }
        }
    }

    private fun navigateToEmpDashboardScreen() {
        startAnotherActivity(Intent(this, EmpDashboardActivity::class.java))
    }

    private fun navigateToDashboardScreen() {
        startAnotherActivity(Intent(this, DashboardActivity::class.java))
    }

    private fun navigateToLoginScreen() {
        startAnotherActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToSelectUlbScreen() {
        startAnotherActivity(Intent(this, AddUlbActivity::class.java))
    }

    private fun startAnotherActivity(intent: Intent) {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            finish()
        }, SPLASH_DELAY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume update if interrupted
        checkForImmediateUpdate()
    }
}
