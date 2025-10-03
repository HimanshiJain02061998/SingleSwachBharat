package com.appynitty.kotlinsbalibrary.housescanify.ui.empSyncOffline

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.MyApplication
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedActivity
import com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail.WorkHistoryDetailActivity
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.CustomAlertDialog
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityEmpSyncOfflineBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.GarbageCollectionAdapter
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.HistoryClickListener
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline.SyncOfflineData
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpGcDao
import com.appynitty.kotlinsbalibrary.housescanify.dao.EmpHouseOnMapDao
import com.appynitty.kotlinsbalibrary.housescanify.model.request.EmpGarbageCollectionRequest
import com.appynitty.kotlinsbalibrary.housescanify.repository.EmpGcRepository
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EmpSyncOfflineActivity : AppCompatActivity(), HistoryClickListener {

    private lateinit var binding: ActivityEmpSyncOfflineBinding

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
    private var isInternetOn = false
    private lateinit var garbageCollectionAdapter: GarbageCollectionAdapter
    private val syncOfflineList = ArrayList<EmpGarbageCollectionRequest>()
    private var totalOfflineCount: Int? = null
    private var alertDialog: AlertDialog? = null
    private var remainingCountTv: TextView? = null
    private var languageId: String? = null
    private var totalGcCount = 0
    private var isSyncingOn = false
    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)

    private val serverDateFormat =
        SimpleDateFormat(DateTimeUtils.GIS_DATE_TIME_FORMAT, Locale.ENGLISH)

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

        binding = ActivityEmpSyncOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        initToolbar()
        subscribeLiveData()
        registerClickEvents()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    private fun registerClickEvents() {

        binding.syncOfflineBtn.setOnClickListener {

            if (syncOfflineList.isNotEmpty()) {
                empSyncGcViewModel.saveGarbageCollectionOfflineDataToApi(
                    CommonUtils.APP_ID,
                    CommonUtils.CONTENT_TYPE
                )
                showCountDialog()

            }
        }

        alertDialog!!.setOnCancelListener {

            if (isSyncingOn) {
                showCountDialog()
                binding.syncOfflineBtn.visibility = View.GONE

            } else {
                if (alertDialog?.isShowing == true)
                    alertDialog?.dismiss()
            }
        }

        alertDialog!!.setOnKeyListener { _: DialogInterface?, keyCode: Int, keyEvent: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                alertDialog!!.dismiss()
                //  finish();
            }
            true
        }

    }

    private fun subscribeLiveData() {

        empSyncGcViewModel.isSyncingOnLiveData.observe(this) {

            isSyncingOn = it

            if (it) {
                showCountDialog()
            } else {
                if (alertDialog?.isShowing == true)
                    alertDialog?.dismiss()
            }
        }

        lifecycleScope.launch {
            totalGcCount = empSyncGcViewModel.getGcCount()
        }

        val snackBar = Snackbar
            .make(
                binding.parent,
                resources.getString(R.string.no_internet_error),
                Snackbar.LENGTH_INDEFINITE
            )

        val internetConnectivity = ConnectivityStatus(this)

        internetConnectivity.observe(this) {
            isInternetOn = it
            if (it) {
                snackBar.dismiss()
                if (totalGcCount > 0) {
                    binding.syncOfflineBtn.visibility = View.VISIBLE

                } else {
                    binding.syncOfflineBtn.visibility = View.GONE
                }

            } else {
                snackBar.show()
                binding.syncOfflineBtn.visibility = View.GONE
            }
        }


        empSyncGcViewModel.getGarbageCollectionListFromRoom().asLiveData().observe(this) {

            totalGcCount = it.size
            if (it.isNotEmpty()) {

                syncOfflineList.clear()
                syncOfflineList.addAll(it)
                binding.syncOfflineBtn.isEnabled = true

                totalOfflineCount = it.size
                val stringBuffer = StringBuffer()
                stringBuffer.append(totalOfflineCount)
                stringBuffer.append(" ")
                stringBuffer.append(resources.getString(R.string.remaining))
                remainingCountTv?.text = stringBuffer

                if (isInternetOn) {
                    if (alertDialog != null && alertDialog!!.isShowing)
                        binding.syncOfflineBtn.visibility = View.GONE
                    else
                        binding.syncOfflineBtn.visibility = View.VISIBLE
                } else
                    binding.syncOfflineBtn.visibility = View.GONE

                Log.d("TAG", "subscribeLiveData: $it")
            } else {

                if (alertDialog != null)
                    if (alertDialog!!.isShowing)
                        alertDialog?.dismiss()
                binding.syncOfflineBtn.visibility = View.GONE
                binding.showErrorOfflineData.visibility = View.VISIBLE
            }
            prepareData(it)
        }
        empSyncGcViewModel.empGcOfflineResponseLiveData.observe(this) {
            when (it) {
                is ApiResponseListener.Loading -> {

                }

                is ApiResponseListener.Success -> {
                    CustomToast.showSuccessToast(this, "Batch Synced Successfully")
                }

                is ApiResponseListener.Failure -> {
                    binding.syncOfflineBtn.visibility = View.VISIBLE
                    isSyncingOn = false
                    if (alertDialog != null)
                        if (alertDialog!!.isShowing)
                            alertDialog?.dismiss()

                    CustomToast.showErrorToast(this, it.message.toString())
                }

                null -> {
                    //to take care
                }
            }
        }
    }

    private fun showCountDialog() {
        // ALERT DIALOG CREATION TO SHOW SYNCING STATUS

        val stringBuffer = StringBuffer()
        stringBuffer.append(totalOfflineCount)
        stringBuffer.append(" ")
        stringBuffer.append(resources.getString(R.string.remaining))
        remainingCountTv?.text = stringBuffer

        if (!alertDialog!!.isShowing)
            alertDialog?.show()

        binding.syncOfflineBtn.visibility = View.GONE
    }

    private fun initToolbar() {

        binding.toolbar.title = resources.getString(R.string.title_activity_sync_offline)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun initVars() {

        languageId = intent.getStringExtra("languageId")

        val empGcViewModelFactory =
            EmpGcViewModelFactory(
                application,
                empGcDao,
                empGcRepository,
                archivedDao,
                houseOnMapDao
            )

        empSyncGcViewModel =
            ViewModelProvider(
                MyApplication.instance,
                empGcViewModelFactory
            )[EmpSyncGcViewModel::class.java]

        garbageCollectionAdapter = GarbageCollectionAdapter()
        garbageCollectionAdapter.setHistoryCardClickListener(this)


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = garbageCollectionAdapter


        // ALERT DIALOG CREATION TO SHOW SYNCING STATUS
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        alertDialog = builder.create()
        val view: View? = CustomAlertDialog.showUploadingDialog(this)
        alertDialog?.setView(view)
        if (view != null) {
            remainingCountTv = view.findViewById(R.id.remaining_count_tv)
        }

    }


    //searching by date
    private fun prepareData(mList: List<EmpGarbageCollectionRequest>) {

        Log.i("OfflineListSize", "prepareData: ${mList.size}")
        val offlineSyncShowList = ArrayList<SyncOfflineData>()

        var offlineGarbageCollectionCount: Int
        var offlineDumpCount: Int
        var streetCount: Int
        var liquidCount: Int
        var masterIdCount: Int
        val controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation)


        offlineSyncShowList.clear()

        if (mList.isNotEmpty()) {
            Log.i("OfflineListSize", "prepareData:2 ${mList.size}")

            mList.forEach {

                val gcDate: String = serverDateFormat.parse(it.date)
                    ?.let { it1 ->
                        dateFormat.format(it1).toString()
                    }.toString()

                var temp = -1
                if (offlineSyncShowList.isNotEmpty()) {
                    temp = offlineSyncShowList.indexOfFirst { syncOfflineData ->
                        syncOfflineData.date == gcDate
                    }
                }
                if (temp >= 0) {
                    if (it.gcType == "3") {
                        offlineSyncShowList[temp].offlineDumpCount++
                    } else {

                        offlineGarbageCollectionCount = 0
                        offlineDumpCount = 0
                        liquidCount = 0
                        streetCount = 0
                        masterIdCount = 0
                        Log.i("HouseScanifyGcType", "prepareData: ${it.gcType}")
                        when (it.gcType) {
                            "1" -> {
                                offlineSyncShowList[temp].offlineGarbageCollectionCount++
                            }

                            "4" -> {
                                offlineSyncShowList[temp].liquidCount++
                            }

                            "5" -> {
                                offlineSyncShowList[temp].streetCount++
                            }
                        }
                    }
                } else {

                    offlineGarbageCollectionCount = 0
                    offlineDumpCount = 0
                    liquidCount = 0
                    streetCount = 0
                    masterIdCount = 0
                    Log.i("HouseScanifyGcType", "prepareData: ${it.gcType}")
                    when (it.gcType) {
                        "3" -> {
                            offlineDumpCount++
                        }

                        "1" -> {
                            offlineGarbageCollectionCount++
                        }

                        "4" -> {
                            liquidCount++
                        }

                        "5" -> {
                            streetCount++
                        }

                        "12" -> {
                            masterIdCount++
                        }
                    }

                    offlineSyncShowList.add(
                        SyncOfflineData(
                            1,
                            gcDate,
                            offlineGarbageCollectionCount,
                            offlineDumpCount,
                            streetCount,
                            liquidCount, 0, masterIdCount
                        )
                    )
                }

            }
        }

        binding.progressBar.visibility = View.GONE
        garbageCollectionAdapter.submitList(null)
        Log.d("TAG", "prepareData: $offlineSyncShowList")
        garbageCollectionAdapter.submitList(offlineSyncShowList) {
            binding.recyclerView.layoutAnimation = controller
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)

            R.id.action_archived -> {
                if (!isSyncingOn) {
                    val intent = Intent(this, ArchivedActivity::class.java)
                    intent.putExtra("languageId", languageId)
                    startActivity(intent)
                    overridePendingTransition(
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.archived_menu, menu)
        return true
    }

    override fun onHistoryCardClicked(date: String) {
        if (!isSyncingOn) {
            val houseDetailsList = arrayListOf<WorkHistoryDetailsResponse>()
            DateTimeUtils.getGisServiceTimeStamp()

            val result = syncOfflineList.sortedByDescending {
                LocalDate.parse(
                    it.date,
                    DateTimeFormatter.ofPattern(DateTimeUtils.GIS_DATE_TIME_FORMAT)
                )
            }

            val tempLIst = result.asReversed()

            tempLIst.forEach {

                val gcDate: String = serverDateFormat.parse(it.date)
                    ?.let { it1 ->
                        dateFormat.format(it1).toString()
                    }.toString()

                if (gcDate == date) {

                    val time: String

                    val dateTime: LocalDateTime = LocalDateTime.parse(
                        it.date,
                        DateTimeFormatter.ofPattern(DateTimeUtils.GIS_DATE_TIME_FORMAT)
                    )

                    val localTime: LocalTime = dateTime.toLocalTime()
                    time = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH).format(localTime)
                        .toString()

                    houseDetailsList.add(
                        WorkHistoryDetailsResponse(
                            time,
                            it.referenceId,
                            null,
                            null,
                            null,
                            it.gcType
                        )
                    )

                }
            }

            val intent =
                Intent(this@EmpSyncOfflineActivity, WorkHistoryDetailActivity::class.java)
            intent.putExtra("isWorkHistoryDetail", false)
            intent.putParcelableArrayListExtra("houseDetailsList", houseDetailsList)
            intent.putExtra("fdate", date)
            startActivity(intent)

        }
    }
}