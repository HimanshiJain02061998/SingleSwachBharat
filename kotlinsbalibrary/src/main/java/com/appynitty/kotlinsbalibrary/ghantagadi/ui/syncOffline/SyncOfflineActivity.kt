package com.appynitty.kotlinsbalibrary.ghantagadi.ui.syncOffline

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
import com.appynitty.kotlinsbalibrary.common.ui.archived.ArchivedActivity
import com.appynitty.kotlinsbalibrary.common.ui.workHistoryDetail.WorkHistoryDetailActivity
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.dialogs.CustomAlertDialog
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivitySyncOfflineBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.blockchain.TripRepository
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.ArchivedDao
import com.appynitty.kotlinsbalibrary.ghantagadi.dao.GarbageCollectionDao
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.GarbageCollectionData
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.WorkHistoryDetailsResponse
import com.appynitty.kotlinsbalibrary.ghantagadi.repository.GarbageCollectionRepo
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


private const val TAG = "SyncOfflineActivity"

@AndroidEntryPoint
class SyncOfflineActivity : AppCompatActivity(), HistoryClickListener {

    private lateinit var binding: ActivitySyncOfflineBinding

    //garbage viewModel has an application scope as it is used for syncing functionality
    //it is required in two activities dashboard and sync offline
    @Inject
    lateinit var garbageCollectionDao: GarbageCollectionDao

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    @Inject
    lateinit var tripRepository: TripRepository

    @Inject
    lateinit var archivedDao: ArchivedDao

    @Inject
    lateinit var garbageCollectionRepo: GarbageCollectionRepo
    private lateinit var garbageCollectionViewModel: GarbageCollectionViewModel

    private var totalOfflineCount: Int? = null
    private lateinit var garbageCollectionAdapter: GarbageCollectionAdapter
    private lateinit var empType: String
    private lateinit var userTypeId: String
    private val syncOfflineList = ArrayList<GarbageCollectionData>()
    private var alertDialog: AlertDialog? = null
    private var remainingCountTv: TextView? = null
    private var isInternetOn = false
    private var languageId: String? = null
    private var isSyncingOn = false
    private var totalGcCount = 0
    //  private var batchCount = 1

    private val dateFormat =
        SimpleDateFormat(DateTimeUtils.SYNC_OFFLINE_DATE_FORMAT, Locale.ENGLISH)
    private val serverDateFormat =
        SimpleDateFormat(DateTimeUtils.SERVER_DATE_TIME_FORMAT_LOCAL, Locale.ENGLISH)


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

        binding = ActivitySyncOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        initToolbar()
        subscribeLiveData()
        registerClickEvents()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)

    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_sync_offline)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun initVars() {

        empType = intent.getStringExtra("empType").toString()
        userTypeId = intent.getStringExtra("userTypeId").toString()
        languageId = intent.getStringExtra("languageId")

        garbageCollectionAdapter = GarbageCollectionAdapter()
        garbageCollectionAdapter.empType = empType
        garbageCollectionAdapter.setHistoryCardClickListener(this)


        val garbageCollectionViewModelFactory = GarbageCollectionViewModelFactory(
            application,
            garbageCollectionRepo,
            garbageCollectionDao,
            archivedDao,
            tripRepository,
            sessionDataStore
        )

        garbageCollectionViewModel = ViewModelProvider(
            com.appynitty.kotlinsbalibrary.common.MyApplication.instance,
            garbageCollectionViewModelFactory
        )[GarbageCollectionViewModel::class.java]

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = garbageCollectionAdapter
        binding.recyclerView.itemAnimator = null


        // ALERT DIALOG CREATION TO SHOW SYNCING STATUS
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        alertDialog = builder.create()
        val view: View? = CustomAlertDialog.showUploadingDialog(this)
        alertDialog?.setView(view)
        if (view != null) {
            remainingCountTv = view.findViewById(R.id.remaining_count_tv)
        }

        lifecycleScope.launch {
            totalGcCount = garbageCollectionViewModel.getGcCount()
        }
    }

    private fun registerClickEvents() {

        alertDialog!!.setOnCancelListener {

            if (isSyncingOn) {
                showCountDialog()
                binding.syncOfflineBtn.visibility = View.GONE

            } else {
                if (alertDialog?.isShowing == true) alertDialog?.dismiss()
            }
        }


        alertDialog!!.setOnKeyListener { _: DialogInterface?, keyCode: Int, keyEvent: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                alertDialog!!.dismiss()
                //  finish();
            }
            true
        }

        binding.syncOfflineBtn.setOnClickListener {

            sessionDataStore
            if (syncOfflineList.isNotEmpty()) {

                garbageCollectionViewModel.saveGarbageCollectionOfflineDataToApi(
                    CommonUtils.APP_ID,
                    userTypeId,
                    CommonUtils.getBatteryStatus(application),
                    CommonUtils.CONTENT_TYPE,
                )

            }

        }
    }

    private fun showCountDialog() {

        val stringBuffer = StringBuffer()
        stringBuffer.append(totalOfflineCount)
        stringBuffer.append(" ")
        stringBuffer.append(resources.getString(R.string.remaining))
        remainingCountTv?.text = stringBuffer

        if (!alertDialog!!.isShowing) alertDialog?.show()

        binding.syncOfflineBtn.visibility = View.GONE
    }

    private fun subscribeLiveData() {

        garbageCollectionViewModel.isSyncingOnLiveData.observe(this) {

            isSyncingOn = it

            if (it) {
                showCountDialog()
            } else {
                if (alertDialog?.isShowing == true) alertDialog?.dismiss()
            }
        }

        val snackBar = Snackbar.make(
            binding.parent,
            resources.getString(R.string.no_internet_error),
            Snackbar.LENGTH_INDEFINITE
        )

        val internetConnectivity = ConnectivityStatus(this)

        internetConnectivity.observe(this) {
            isInternetOn = it
            if (it) {
                snackBar.dismiss()
                Log.i("TotalGcCount", "subscribeLiveData: $totalGcCount")
                if (totalGcCount > 0) {
                    binding.syncOfflineBtn.visibility = View.VISIBLE
                } else {
                    binding.syncOfflineBtn.visibility = View.GONE
                }

                //sync dump trip blockchain
                // garbageCollectionViewModel.syncDumpYardTrip()
            } else {
                snackBar.show()
                binding.syncOfflineBtn.visibility = View.GONE
            }
        }


        garbageCollectionViewModel.getGarbageCollectionListFromRoom().asLiveData().observe(this) {

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
                    if (alertDialog != null && alertDialog!!.isShowing) binding.syncOfflineBtn.visibility =
                        View.GONE
                    else {
                        if (it.isNotEmpty()) binding.syncOfflineBtn.visibility = View.VISIBLE
                    }

                } else binding.syncOfflineBtn.visibility = View.GONE

                Log.d("TAG", "subscribeLiveData: $it")
            } else {
                if (alertDialog != null) if (alertDialog!!.isShowing) alertDialog?.dismiss()
                binding.syncOfflineBtn.visibility = View.GONE
                binding.showErrorOfflineData.visibility = View.VISIBLE

            }
            prepareData(it)
        }

        garbageCollectionViewModel.garbageCollectionResponseLiveData.observe(this) {

            when (it) {

                is ApiResponseListener.Loading -> {

                }

                is ApiResponseListener.Success -> {
                    CustomToast.showSuccessToast(this, "Batch Synced Successfully")
                    // batchCount++
                }

                is ApiResponseListener.Failure -> {
                    if (isInternetOn) binding.syncOfflineBtn.visibility = View.VISIBLE
                    if (alertDialog != null) if (alertDialog!!.isShowing) alertDialog?.dismiss()

                    CustomToast.showErrorToast(this, it.message.toString())
                    garbageCollectionViewModel.setSyncingLiveDataToNull()
                }

                null -> {
                    //take care
                    if (isInternetOn) {
                        if (totalOfflineCount!! > 0) {
                            lifecycleScope.launch {
                                val count = garbageCollectionViewModel.getGcCount()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (count > 0) binding.syncOfflineBtn.visibility = View.VISIBLE
                                }
                            }
                        } else binding.syncOfflineBtn.visibility = View.INVISIBLE
                    } else binding.syncOfflineBtn.visibility = View.INVISIBLE

                }
            }
        }
    }

    //searching by date
    private fun prepareData(mList: List<GarbageCollectionData>) {

        val offlineSyncShowList = ArrayList<SyncOfflineData>()
        var offlineGarbageCollectionCount: Int
        var offlineDumpCount: Int
        val controller =
            AnimationUtils.loadLayoutAnimation(this@SyncOfflineActivity, R.anim.layout_animation)

        if (mList.isNotEmpty()) {

            mList.forEach {
                val gcDate: String = serverDateFormat.parse(it.gcDate)?.let { it1 ->
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
                        offlineSyncShowList[temp].offlineGarbageCollectionCount++
                    }
                } else {
                    offlineGarbageCollectionCount = 0
                    offlineDumpCount = 0
                    if (it.gcType == "3") {
                        offlineDumpCount++
                    } else {
                        offlineGarbageCollectionCount++
                    }

                    offlineSyncShowList.add(
                        SyncOfflineData(
                            0, gcDate, offlineGarbageCollectionCount, offlineDumpCount, 0, 0, 0, 0
                        )
                    )
                }

            }
        }
        binding.progressBar.visibility = View.GONE
        garbageCollectionAdapter.empType = empType
        garbageCollectionAdapter.submitList(null)

        garbageCollectionAdapter.submitList(offlineSyncShowList) {
            binding.recyclerView.layoutAnimation = controller
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(
                this
            )

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
        Log.d("TAG", "onHistoryCardClicked: $date")

        if (!isSyncingOn) {
            val houseDetailsList = arrayListOf<WorkHistoryDetailsResponse>()
            val result = syncOfflineList.sortedByDescending {
                LocalDate.parse(
                    it.gcDate,
                    DateTimeFormatter.ofPattern(DateTimeUtils.SERVER_DATE_TIME_FORMAT_LOCAL)
                )
            }

            val tempLIst = result.asReversed()

            tempLIst.forEach {

                val gcDate: String = serverDateFormat.parse(it.gcDate)?.let { it1 ->
                    dateFormat.format(it1).toString()
                }.toString()

                if (gcDate == date) {

                    val time: String

                    val dateTime: LocalDateTime = LocalDateTime.parse(
                        it.gcDate,
                        DateTimeFormatter.ofPattern(DateTimeUtils.SERVER_DATE_TIME_FORMAT_LOCAL)
                    )
                    val localTime: LocalTime = dateTime.toLocalTime()
                    time = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH).format(localTime)
                        .toString()

                    if (it.gcType == "6") {
                        houseDetailsList.add(
                            WorkHistoryDetailsResponse(
                                time, it.referenceId, null, null, null, it.gcType
                            )
                        )
                    } else {
                        houseDetailsList.add(
                            WorkHistoryDetailsResponse(
                                time, it.referenceId, null, it.vehicleNumber, null, it.gcType
                            )
                        )
                    }


                }
            }

            val intent = Intent(this@SyncOfflineActivity, WorkHistoryDetailActivity::class.java)
            intent.putExtra("isWorkHistoryDetail", false)
            intent.putParcelableArrayListExtra("houseDetailsList", houseDetailsList)
            intent.putExtra("fdate", date)
            startActivity(intent)
        }
    }

}