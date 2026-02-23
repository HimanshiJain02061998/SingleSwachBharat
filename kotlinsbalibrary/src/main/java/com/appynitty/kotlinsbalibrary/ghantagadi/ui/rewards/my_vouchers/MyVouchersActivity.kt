package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.MonthYearPickerDialogFragment
import com.appynitty.kotlinsbalibrary.databinding.ActivityMyVouchersBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class MyVouchersActivity : AppCompatActivity(), OnDateSelectedListener {
    private lateinit var binding: ActivityMyVouchersBinding
    private lateinit var vouchersPagerAdapter: VouchersPagerAdapter
    private var walletId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyVouchersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initActivity()
    }

    private fun initActivity() {
        walletId = intent.getStringExtra("walletId") ?: ""
        setupToolBar()
        setupTabs()

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

                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = formatter.format(selectedDate)
                        onDateSelected(formattedDate)
                    }
                })

                picker.show(supportFragmentManager, "monthYearPicker")
            }
        }
    }

    fun getWalletId(): String {
        return walletId.toString()
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

    private fun setupTabs() {
        vouchersPagerAdapter = VouchersPagerAdapter(this)
        binding.vouchersViewPager.adapter = vouchersPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.vouchersViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.active) //"Active"
                1 -> getString(R.string.claimed) //"Claim"
                2 -> getString(R.string.expired) //"Expired"
                else -> "Active"
            }
        }.attach()
    }

    override fun onDateSelected(formattedDate: String) {
        val currentPosition = binding.vouchersViewPager.currentItem

        val currentFragment = supportFragmentManager.findFragmentByTag("f$currentPosition")

        if (currentFragment != null && currentFragment.isAdded && currentFragment.isVisible) {
            when (currentFragment) {
                is ActiveVouchersFrag -> {
                    Timber.d("Current fragment: ActiveVouchersFrag")
                    currentFragment.updateDate(formattedDate)
                }

                is ClaimVouchersFrag -> {
                    Timber.d("Current fragment: ClaimVouchersFrag")
                    currentFragment.updateDate(formattedDate)
                }

                is ExpiredVouchersFrag -> {
                    Timber.d("Current fragment: ExpiredVouchersFrag")
                    currentFragment.updateDate(formattedDate)
                }
            }
        } else {
            Timber.w("Fragment is not attached, cannot update date")
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.pointerCount == 1 && super.dispatchTouchEvent(ev)
    }
}

interface OnDateSelectedListener {
    fun onDateSelected(formattedDate: String)
}