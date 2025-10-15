package com.appynitty.kotlinsbalibrary.common.ui.addUlb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.SelectCommonActivity
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.DistCityType
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.viewModel.AddUlbViewModel
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.LanguageBottomSheetFrag
import com.appynitty.kotlinsbalibrary.databinding.ActivityAddUlbBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class AddUlbActivity : AppCompatActivity(),LanguageBottomSheetFrag.LanguageDialogCallbacks {
    private val viewModel: AddUlbViewModel by viewModels()
    private lateinit var binding: ActivityAddUlbBinding
    private var isInternetOn = false
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private lateinit var languageDataStore: LanguageDataStore
    var ulbId = -1
    var ulbName = ""
    var distName = ""
    var distId = -1
    private lateinit var languageBottomSheet: LanguageBottomSheetFrag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddUlbBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        languageBottomSheet = LanguageBottomSheetFrag()
        binding.rlUlb.isEnabled = false
        subscribeLiveData()
        setOnClickListner()
        subscribeChannelEvent()
    }

    private val pickCityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            if (data?.getStringExtra(DistCityType.Type.type)==DistCityType.Dist.type){
                binding.rlUlb.isEnabled = true
                distName =  data?.getStringExtra(DistCityType.Name.type) ?: ""
                distId = data?.getIntExtra(DistCityType.Id.type,-1)   ?: -1
                binding.tvDist.text = distName
                binding.tvUlb.text = ""
                ulbName = ""
                ulbId = -1
                } else {
                ulbName =  data?.getStringExtra(DistCityType.Name.type) ?: ""
                ulbId = data?.getIntExtra(DistCityType.Id.type,-1)   ?: -1
                binding.tvUlb.text = ulbName
            }

        }
    }

    private fun setOnClickListner() {
        binding.rlDist.setOnClickListener {
            val intent = Intent(this, SelectCommonActivity::class.java)
            intent.putExtra(DistCityType.Type.type, DistCityType.Dist.type)
            intent.putExtra(DistCityType.Id.type, -1)
            pickCityLauncher.launch(intent)
        }
        binding.rlUlb.setOnClickListener {
            val intent = Intent(this, SelectCommonActivity::class.java)
            intent.putExtra(DistCityType.Type.type, DistCityType.Ulb.type)
            intent.putExtra(DistCityType.Id.type, distId)
            pickCityLauncher.launch(intent)
        }
        binding.btnGoAhead.setOnClickListener {
            if (viewModel.validateUlb(
                    binding.tvDist.text.toString().trim(),
                    binding.tvUlb.text.toString().trim()
                )
            ) {
                viewModel.selectUlb(ulbId.toString(), ulbName)
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

    private fun subscribeLiveData() {
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
        }

    }


    private fun subscribeChannelEvent() {

        lifecycleScope.launch {
            viewModel.addUlbEventsFlow.collect { event ->
                when (event) {
                    AddUlbViewModel.AddUlbEvent.NavigateToLogin -> {
                        startActivity(Intent(this@AddUlbActivity, LoginActivity::class.java))
                        finish()
                    }

                    is AddUlbViewModel.AddUlbEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@AddUlbActivity, event.msg)
                    }

                    is AddUlbViewModel.AddUlbEvent.ShowSuccessMessage -> {
                        CustomToast.showSuccessToast(
                            this@AddUlbActivity, resources.getString(event.resourceId)
                        )
                    }

                    is AddUlbViewModel.AddUlbEvent.ShowFailureMessageRes -> {
                        CustomToast.showErrorToast(
                            this@AddUlbActivity, resources.getString(event.resourceId)
                        )
                    }
                }
            }
        }

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) return true
        return super.dispatchTouchEvent(ev)
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