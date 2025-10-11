package com.appynitty.kotlinsbalibrary.ghantagadi.ui.workHistory

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication.Companion.APP_ID
import com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail.WorkHistoryDetailActivity
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityWorkHistoryBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.GarbageCollectionAdapter
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.HistoryClickListener
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.SyncOfflineData
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


private const val TAG = "WorkHistoryActivity"

@AndroidEntryPoint
class WorkHistoryActivity : AppCompatActivity(), HistoryClickListener {

    private val viewModel: WorkHistoryViewModel by viewModels()

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    private lateinit var binding: ActivityWorkHistoryBinding
    private var userId: String? = null
    private var empType: String? = null
    private lateinit var adapter: GarbageCollectionAdapter
    private var monthPosition: Int = CommonUtils.getCurrentMonth()
    private var isInternetOn = false
    private var isInternetOnAndCanFetch = false
    private lateinit var internetConnectivity: ConnectivityStatus

    //multi language functionality
    override fun attachBaseContext(newBase: Context?) {

        var context: Context? = newBase
        if (newBase != null) {
            val languageDataStore = LanguageDataStore(newBase.applicationContext)
            //getting language data from datastore
            val appLanguage = languageDataStore.currentLanguage
            context = newBase.let { LanguageConfig.changeLanguage(it, appLanguage.languageId) }

        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityWorkHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initVars()
        subscribeLiveData()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    private fun initVars() {

        empType = intent.getStringExtra("empType")
        userId = intent.getStringExtra("userId")

        internetConnectivity = ConnectivityStatus(this)

        binding.workHistoryRecyclerView.setHasFixedSize(true)
        binding.workHistoryRecyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = GarbageCollectionAdapter()
        adapter.setHistoryCardClickListener(this)
        binding.workHistoryRecyclerView.adapter = adapter
        binding.workHistoryRecyclerView.itemAnimator = null


    }

    override fun onResume() {
        super.onResume()
        initSpinner()
        if (APP_ID == "3201")
            showMasterPlateUpdateDialogIfNeeded()
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_history_page)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun initSpinner() {

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
                CommonUtils.APP_ID, userId!!, year, month, empType!!
            )
    }

    private fun setAdapterList(historyList: List<WorkHistoryResponse>) {
        val modifiedHistoryList = ArrayList<SyncOfflineData>()
        val requiredDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH)


        historyList.forEach {

            val count =
                if (it.houseCollection != "0") it.houseCollection else if (it.StreetCollection != "0") it.StreetCollection
                else if (it.DumpYardPlantCollection != "0") it.DumpYardPlantCollection
                else if (it.LiquidCollection != "0") it.LiquidCollection else "0"

            if (count != null) {
                it.date?.let { it1 ->
                    val gcDate = dateFormat.parse(it1)

                    it.DumpYardCollection?.let { it2 ->
                        gcDate?.let { it3 -> requiredDateFormat.format(it3).toString() }
                            ?.let { it4 ->
                                SyncOfflineData(
                                    0, it4, count.toInt(), it2.toInt(), 0, 0, 0, 0
                                )
                            }
                    }
                }?.let { it2 -> modifiedHistoryList.add(it2) }
            }
        }

        adapter.empType = empType
        adapter.submitList(null)
        adapter.submitList(modifiedHistoryList) {
            val controller = AnimationUtils.loadLayoutAnimation(
                this@WorkHistoryActivity,
                R.anim.layout_animation
            )
            binding.workHistoryRecyclerView.layoutAnimation = controller
        }

    }

    private fun subscribeLiveData() {

        val snackBar = Snackbar
            .make(
                binding.parent,
                resources.getString(R.string.no_internet_error),
                Snackbar.LENGTH_INDEFINITE
            )


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
                binding.noHistoryFound.visibility = View.GONE
            }
            isInternetOn = it
        }

        viewModel.workHistoryResponseResponseLiveData.observe(this) {
            when (it) {

                is ApiResponseListener.Loading -> {
                    binding.historyProgressBar.visibility = View.VISIBLE
                }

                is ApiResponseListener.Success -> {
                    Log.d(TAG, "subscribeLiveData: $it")

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

    override fun onHistoryCardClicked(date: String) {

        val intent = Intent(this, WorkHistoryDetailActivity::class.java)
        intent.putExtra("fdate", date)
        intent.putExtra("userId", userId)
        intent.putExtra("userType", 0)
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

    private fun showMasterPlateUpdateDialogIfNeeded() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val titleView = TextView(this).apply {
            text = getString(R.string.alert)
            gravity = Gravity.CENTER
            setTextColor(Color.RED)
            textSize = 20f
            setPadding(0, 30, 0, 30)
        }

        if (hour in 4..20) {
            AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setMessage(
                    R.string.note_that_associated
                )
                .setPositiveButton(R.string.ok_txt) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

}