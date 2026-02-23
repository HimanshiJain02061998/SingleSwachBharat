package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.transaction_history

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.MonthYearPickerDialogFragment
import com.appynitty.kotlinsbalibrary.databinding.ActivityTransactionHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionHistoryBinding

    private val transactionVM: TransactionHistoryVM by viewModels()

    private val historyAdapter: TransactionHistoryAdapter by lazy {
        TransactionHistoryAdapter(emptyList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initActivity(savedInstanceState)
    }

    private fun initActivity(savedInstanceState: Bundle?) {
        val walletId = intent.getStringExtra("walletId")
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate: String = formatter.format(Date())

        if (savedInstanceState == null) {
            transactionVM.getTransactionHistory(walletId!!, currentDate)
        }

        setupToolBar()
        setupRecyclerView()
        subscribeChannelEvents()

        binding.ivCalendar.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag("monthYearPicker")
            if (fragment == null) {
                val picker = MonthYearPickerDialogFragment()

                picker.setListener(object : MonthYearPickerDialogFragment.OnDateSetListener {
                    override fun onDateSet(year: Int, month: Int) {
                        val selectedDate = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                        }.time

                        val formattedDate = formatter.format(selectedDate)
                        transactionVM.getTransactionHistory(
                            walletId ?: "",
                            formattedDate
                        )
                    }
                })

                picker.show(supportFragmentManager, "monthYearPicker")
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvTransactionHistory.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            setHasFixedSize(true)
            adapter = historyAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(
                    DividerItemDecoration(this@TransactionHistoryActivity, RecyclerView.VERTICAL)
                )
            }
        }
    }

    private fun subscribeChannelEvents() {
        lifecycleScope.launch {
            transactionVM.transactionHistoryState.collect { state ->

                when (state) {
                    is TransactionHistoryState.Error -> {
                        binding.pbTransactionHistory.visibility = View.GONE
                        CustomToast.showErrorToast(this@TransactionHistoryActivity, state.message)
                    }

                    is TransactionHistoryState.Loading -> {
                        binding.pbTransactionHistory.visibility = View.VISIBLE
                    }

                    is TransactionHistoryState.Success -> {
                        binding.pbTransactionHistory.visibility = View.GONE
                        val newTransactionList = state.data.transactionList
                        Timber.d("onSuccess: $newTransactionList")

                        historyAdapter.updateData(newTransactionList)

                        if (newTransactionList.isEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setupToolBar() {
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.icn_back)

        val drawable = binding.myToolbar.navigationIcon
        drawable?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, R.color.black))
            binding.myToolbar.navigationIcon = it
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.myToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.pointerCount == 1 && super.dispatchTouchEvent(ev)
    }
}