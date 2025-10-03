package com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityWorkHistoryDetailBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "WorkHistoryDetailActivity"

@AndroidEntryPoint
class WorkHistoryDetailActivity : AppCompatActivity(), WorkHistoryDetailsClickListener {

    private val viewModel: WorkHistoryDetailViewModel by viewModels()

    private lateinit var binding: ActivityWorkHistoryDetailBinding
    private var fDate: String? = null
    private var userId: String? = null
    private var isInternetOn = false
    private var userType: Int? = null
    private lateinit var adapter: WorkHistoryDetailAdapter
    private var isWorkHistoryDetail = false
    private var offlineHouseDetailsList = arrayListOf<WorkHistoryDetailsResponse>()
    private lateinit var internetConnectivity: ConnectivityStatus
    private lateinit var controller: LayoutAnimationController

    //multi language functionality
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

        binding = ActivityWorkHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        BackBtnPressedUtil.handleBackBtnPressed(
            this,
            this,
            this
        )
    }

    private fun initVars() {

        isWorkHistoryDetail = intent.getBooleanExtra("isWorkHistoryDetail", false)
        adapter = WorkHistoryDetailAdapter()
        adapter.setListener(this)
        val date = intent.getStringExtra("fdate")
        if (date != null) {
            val nDate = date.replace("-", " ")
            Log.d(TAG, "initVars: $nDate")
            initToolbar(nDate)
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        controller =
            AnimationUtils.loadLayoutAnimation(this, R.anim.slide_layout_animation)
        binding.recyclerView.adapter = adapter

        if (isWorkHistoryDetail) {

            internetConnectivity = ConnectivityStatus(this)

            val requiredDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

            val gcDate = date?.let { requiredDateFormat.parse(it) }
            fDate = gcDate?.let { dateFormat.format(it).toString() }

            userId = intent.getStringExtra("userId")
            userType = intent.getIntExtra("userType", 0)

            subscribeLiveData()

        } else {

            offlineHouseDetailsList.clear()
            offlineHouseDetailsList = if (Build.VERSION.SDK_INT >= 33) {
                intent?.getParcelableArrayListExtra(
                    "houseDetailsList",
                    WorkHistoryDetailsResponse::class.java
                ) as ArrayList<WorkHistoryDetailsResponse>
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableArrayListExtra(
                    "houseDetailsList"
                )!!
            }

            Log.i(TAG, "initVars: ${offlineHouseDetailsList.size}")
            binding.lineView.visibility = View.VISIBLE
            binding.recyclerView.layoutAnimation = controller
            adapter.submitList(offlineHouseDetailsList)
        }


    }

    private fun initToolbar(date: String) {
        binding.toolbar.title = date
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun subscribeLiveData() {

        val snackBar = Snackbar
            .make(binding.parent, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)


        internetConnectivity.observe(this) {
            isInternetOn = it
            if (it) {
                snackBar.dismiss()
                getHistoryDetailsData()
            } else {
                snackBar.show()
            }
        }

        if (userType == 0) {
            viewModel.workHistoryDetailsResponseLiveData.observe(this) {

                when (it) {
                    is ApiResponseListener.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is ApiResponseListener.Failure -> {
                        binding.progressBar.visibility = View.GONE
                    }

                    is ApiResponseListener.Success -> {
                        if (it.data != null) {
                            if (it.data.isNotEmpty()) {

                                binding.recyclerView.layoutAnimation = controller
                                adapter.submitList(it.data)
                                binding.progressBar.visibility = View.GONE
                                binding.lineView.visibility = View.VISIBLE

                            }
                        }
                    }
                }
            }
        } else if (userType == 1) {

            viewModel.empWorkHistoryDetailsResponseLiveData.observe(this) {

                when (it) {
                    is ApiResponseListener.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is ApiResponseListener.Failure -> {
                        binding.progressBar.visibility = View.GONE

                    }

                    is ApiResponseListener.Success -> {
                        if (it.data != null) {
                            if (it.data.isNotEmpty()) {
                                val mList = ArrayList<WorkHistoryDetailsResponse>()
                                it.data.forEach { response ->

                                    var refId = ""
                                    if (response.HouseNo != null) {
                                        refId = response.HouseNo
                                    }
                                    if (response.DumpYardNo != null) {
                                        refId = response.DumpYardNo
                                    }
                                    if (response.StreetNo != null) {
                                        refId = response.StreetNo
                                    }
                                    if (response.LiquidNo != null) {
                                        refId = response.LiquidNo
                                    }
                                    if (response.MasterPlateNo != null) {
                                        refId = response.MasterPlateNo
                                    }

                                    val workHistoryResponse = WorkHistoryDetailsResponse(
                                        response.time,
                                        refId,
                                        "",
                                        "",
                                        "",
                                        response.type
                                    )
                                    mList.add(workHistoryResponse)
                                }
                                binding.recyclerView.layoutAnimation = controller
                                adapter.submitList(mList)
                                binding.progressBar.visibility = View.GONE
                                binding.lineView.visibility = View.VISIBLE
                            }
                        }
                    }
                }

            }
        }

    }

    private fun getHistoryDetailsData() {

        if (userId != null && fDate != null) {
            if (userType == 0) {
                viewModel.getWorkHistoryDetailList(
                    CommonUtils.APP_ID,
                    userId!!,
                    fDate!!,
                    "1"
                )
            } else if (userType == 1) {

                viewModel.getEmpWorkHistoryDetailList(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE,
                    userId!!,
                    fDate!!
                )
            }
        }

    }

    override fun onTimeBtnClicked(time: String) {
        CustomToast.showSuccessToast(this, time)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(
                this
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "onRestart: ")
    }

}