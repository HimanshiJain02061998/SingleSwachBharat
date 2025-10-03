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
import com.appynitty.kotlinsbalibrary.common.ui.inAppUpdate.UpdateDialogFragment
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.dashboard.DashboardActivity
import com.appynitty.kotlinsbalibrary.housescanify.ui.empDashboard.EmpDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    private lateinit var languageDataStore: LanguageDataStore
    private lateinit var versionCodeTv: TextView
    private lateinit var updateDialogFragment: UpdateDialogFragment
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

        versionCode = (CommonUtils.VERSION_CODE).toInt()
        val versionName = "$versionCode.0"
        updateDialogFragment = UpdateDialogFragment()
        updateDialogFragment.isCancelable = false

        versionCodeTv = findViewById(R.id.versionCodeTv)
        versionCodeTv.text = buildString {
            append("Version : ")
            append(versionName)
        }

        viewModel.checkForUpdate(versionCode)

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
                        navigateToLoginScreen()
                    }

                    is SplashViewModel.SplashEvent.ShowErrorMsg -> {
                        CustomToast.showErrorToast(this@SplashActivity, event.msg)
                    }

                    is SplashViewModel.SplashEvent.ShowUpdateDialog -> {
                        updateDialogFragment.show(supportFragmentManager, UpdateDialogFragment.TAG)
                        updateDialogFragment.downloadLink = event.downloadLink
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

    private fun startAnotherActivity(intent: Intent) {

        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
            finish()
        }, 2000)

    }

}