package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.rewardScreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.GridSpacingItemDecoration
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityRewardsBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Category
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.about_coins.AboutCoinsActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.category_wise_deals.DealsByCategoryActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.my_vouchers.MyVouchersActivity
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.transaction_history.TransactionHistoryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RewardsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRewardsBinding
    private val rewardsViewModel: RewardsViewModel by viewModels()


    private var categoryList: List<Category> = emptyList()
    private lateinit var balancedCoins: String
    private lateinit var walletId: String
    private val adapter by lazy {
        CategoryAdapter(categoryList) { category ->
            Timber.d("Selected Category: ${category.catName}")
            val intent = Intent(this, DealsByCategoryActivity::class.java)
            intent.putExtra("balancedCoins", balancedCoins)
            intent.putExtra("selectedCategory", category.catId)
            intent.putExtra("walletId", walletId)
            intent.putParcelableArrayListExtra("categoryList", ArrayList(categoryList))
            startActivity(intent)
        }
    }

    @Inject
    lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initComponents()
    }

    private fun initComponents() {
        setupToolBar()
        subscribeActivityEvents()
        initOnClickListeners()
        lifecycleScope.launch {
            walletId = userDataStore.getEmpRewardSysInfo.first().walletId
            balancedCoins = userDataStore.getEmpRewardSysInfo.first().balCoins.toString()
            Timber.d("WalletID: $walletId & Coins: $balancedCoins")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                rewardsViewModel.getDealsCategories()
            }
        }

        binding.tvCouponsCount.text = balancedCoins
        binding.tvRewardPoints.text = balancedCoins
    }

    private fun subscribeActivityEvents() {
        lifecycleScope.launch {
            launch {
                rewardsViewModel.rewardsActivityEvent.collect {
                    when (it) {
                        RewardsActivityEvents.HideProgressBar -> {
                            binding.pbRewards.visibility = View.GONE
                        }

                        is RewardsActivityEvents.OnFailure -> {
                            CustomToast.showErrorToast(this@RewardsActivity, it.message)
                        }

                        is RewardsActivityEvents.OnSuccess -> {
                            Timber.d("OnSuccess: " + it.response.message)
                        }

                        RewardsActivityEvents.ShowProgressBar -> {
                            binding.pbRewards.visibility = View.VISIBLE
                        }
                    }
                }
            }

            launch {
                rewardsViewModel.categoriesFlow.collect { categories ->
                    if (!categories.isNullOrEmpty()) {
                        categoryList = categories
                        initRecyclerView(categoryList)
                    } else {
                        rewardsViewModel.getDealsCategories()
                    }
                }
            }
        }
    }

    private fun initOnClickListeners() {
        binding.apply {
            myToolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            tvSeeAll.setOnClickListener {
                startActivity(
                    Intent(
                        this@RewardsActivity,
                        DealsByCategoryActivity::class.java
                    ).apply {
                        putExtra("balancedCoins", balancedCoins)
                        putExtra("selectedCategory", 0)
                        putExtra("walletId", walletId)
                        putParcelableArrayListExtra("categoryList", ArrayList(categoryList))
                    })
            }

            clTransactionHistory.setOnClickListener {
                Timber.d("walletId: $walletId")
                val intent = Intent(this@RewardsActivity, TransactionHistoryActivity::class.java)
                intent.putExtra("walletId", walletId)
                startActivity(intent)
            }

            ivArrowTransaction.setOnClickListener {
                val intent = Intent(this@RewardsActivity, TransactionHistoryActivity::class.java)
                intent.putExtra("walletId", walletId)
                startActivity(intent)
            }

            clMyVouchers.setOnClickListener {
                val intent = Intent(this@RewardsActivity, MyVouchersActivity::class.java)
                intent.putExtra("walletId", walletId)
                startActivity(intent)
            }

            ivArrowMyVouchers.setOnClickListener {
                val intent = Intent(this@RewardsActivity, MyVouchersActivity::class.java)
                intent.putExtra("walletId", walletId)
                startActivity(intent)
            }

            clAboutCoins.setOnClickListener {
                val intent = Intent(this@RewardsActivity, AboutCoinsActivity::class.java)
                startActivity(intent)
            }

            ivArrowAboutCoins.setOnClickListener {
                val intent = Intent(this@RewardsActivity, AboutCoinsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initRecyclerView(categoryList: List<Category>) {
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.categoryRecyclerView.addItemDecoration(GridSpacingItemDecoration(3, 0, true))
        adapter.updateData(categoryList)
        binding.categoryRecyclerView.adapter = adapter
    }

    private fun setupToolBar() {
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawable = binding.myToolbar.navigationIcon
        drawable?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, R.color.black))
            binding.myToolbar.navigationIcon = it
        }

        binding.myToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}