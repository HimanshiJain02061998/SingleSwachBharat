package com.appynitty.kotlinsbalibrary.housescanify.ui.empHistory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail.WorkHistoryDetailActivity
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityEmpHistoryBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.GarbageCollectionAdapter
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.HistoryClickListener
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.SyncOfflineData
import com.appynitty.kotlinsbalibrary.housescanify.model.response.EmpWorkHistoryResponse
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EmpHistoryActivity : AppCompatActivity(), HistoryClickListener {

    private val viewModel: EmpWorkHistoryViewModel by viewModels()

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    private lateinit var binding: ActivityEmpHistoryBinding
    private var userId: String? = null
    private lateinit var adapter: GarbageCollectionAdapter
    private var monthPosition: Int = CommonUtils.getCurrentMonth()
    private var isInternetOn = false
    private var isInternetOnAndCanFetch = false

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

        binding = ActivityEmpHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initVars()
        subscribeLiveData()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    override fun onResume() {
        super.onResume()
        initSpinner()
    }

    private fun subscribeLiveData() {

        val snackBar = Snackbar
            .make(
                binding.parent,
                resources.getString(R.string.no_internet_error),
                Snackbar.LENGTH_INDEFINITE
            )

        val internetConnectivity = ConnectivityStatus(this)
        internetConnectivity.observe(this) {
            Log.i("InternetCheck", "subscribeLiveData: $it")
            isInternetOnAndCanFetch = it
            if (it) {
                snackBar.dismiss()
                if (!isInternetOn)
                    getHistoryFromApi(
                        (monthPosition + 1).toString(), binding.yearSpinner.text.toString()
                    )
            } else {
                snackBar.show()
                binding.historyProgressBar.visibility = View.GONE
            }
            isInternetOn = it
        }

        viewModel.workHistoryResponseLiveData.observe(this) {
            when (it) {

                is ApiResponseListener.Loading -> {
                    binding.historyProgressBar.visibility = View.VISIBLE
                }

                is ApiResponseListener.Success -> {

                    if (it.data?.isEmpty() == true) {
                        binding.noHistoryFound.visibility = View.VISIBLE

                    } else {
                        binding.noHistoryFound.visibility = View.GONE

                    }
                    it.data?.let { it1 -> setAdapterList(it1) }

                    binding.historyProgressBar.visibility = View.GONE

                }

                is ApiResponseListener.Failure -> {
                    CustomToast.showErrorToast(this, it.message.toString())
                    binding.historyProgressBar.visibility = View.GONE

                }
            }
        }

    }

    private fun setAdapterList(historyList: List<EmpWorkHistoryResponse>) {
        val modifiedHistoryList = ArrayList<SyncOfflineData>()
        val requiredDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        historyList.forEach {

            val houseCollection = if (it.houseCollection != null) {
                it.houseCollection!!.toInt()
            } else {
                0
            }
            val streetCollection = if (it.streetCollection != null) {
                it.streetCollection.toInt()
            } else {
                0
            }
            val liquidCollection = if (it.liquidCollection != null) {
                it.liquidCollection.toInt()
            } else {
                0
            }
            val dumpYardCollection = if (it.dumpYardCollection != null) {
                it.dumpYardCollection.toInt()
            } else {
                0
            }
            val masterPlateCollection = if (it.masterPlateCollection != null) {
                it.masterPlateCollection!!
            } else {
                0
            }

            it.date?.let { it1 ->
                val gcDate = dateFormat.parse(it1)
                gcDate?.let { it3 -> requiredDateFormat.format(it3).toString() }
                    ?.let { fDate ->
                        SyncOfflineData(
                            1,
                            fDate,
                            houseCollection,
                            dumpYardCollection,
                            streetCollection,
                            liquidCollection, 0,
                            masterPlateCollection
                        )
                    }

            }?.let { it2 -> modifiedHistoryList.add(it2) }

        }

        adapter.submitList(null)
        adapter.submitList(modifiedHistoryList) {
            val controller =
                AnimationUtils.loadLayoutAnimation(
                    this@EmpHistoryActivity,
                    R.anim.layout_animation
                )
            binding.workHistoryRecyclerView.layoutAnimation = controller
        }
    }

    private fun initSpinner() {

        //month spinner
        val monthAdapter = ArrayAdapter(
            this,
            R.layout.drop_down_item,
            resources.getStringArray(R.array.months)
        )
        binding.monthSpinner.setAdapter(monthAdapter)

        binding.monthSpinner.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources, R.drawable.filter_spinner_dropdown_bg, null
            )
        )

        binding.monthSpinner.setText(
            binding.monthSpinner.adapter.getItem(CommonUtils.getCurrentMonth()).toString(), false
        )

        binding.monthSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                monthPosition = position
                getHistoryFromApi((position + 1).toString(), binding.yearSpinner.text.toString())
            }

        //year spinner
        val yearAdapter = ArrayAdapter(
            this, R.layout.drop_down_item, CommonUtils.getYearList()
        )
        binding.yearSpinner.setAdapter(yearAdapter)
        binding.yearSpinner.setDropDownBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources, R.drawable.filter_spinner_dropdown_bg, null
            )
        )
        binding.yearSpinner.setText(binding.yearSpinner.adapter.getItem(0).toString(), false)

        binding.yearSpinner.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            getHistoryFromApi(
                (monthPosition + 1).toString(), binding.yearSpinner.text.toString()
            )
        }
    }

    private fun getHistoryFromApi(month: String, year: String) {

        if (isInternetOnAndCanFetch)
            viewModel.getWorkHistoryList(
                CommonUtils.APP_ID, CommonUtils.CONTENT_TYPE, userId!!, year, month
            )

    }

    private fun initVars() {

        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)
        userId = intent.getStringExtra("userId")
        binding.workHistoryRecyclerView.setHasFixedSize(true)
        binding.workHistoryRecyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = GarbageCollectionAdapter()
        adapter.setHistoryCardClickListener(this)
        binding.workHistoryRecyclerView.adapter = adapter
        binding.workHistoryRecyclerView.itemAnimator = null
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_history_page)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onHistoryCardClicked(date: String) {

        val intent = Intent(this, WorkHistoryDetailActivity::class.java)
        intent.putExtra("fdate", date)
        intent.putExtra("userId", userId)
        intent.putExtra("userType", 1)
        intent.putExtra("isWorkHistoryDetail", true)
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_right, R.anim.slide_out_left
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)
        }
        return super.onOptionsItemSelected(item)
    }


}