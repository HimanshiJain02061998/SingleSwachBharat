package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.category_wise_deals

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.GridSpacingItemDecoration
import com.appynitty.kotlinsbalibrary.databinding.ActivityDealsCategoryBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.Category
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.redeem.RedeemActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DealsByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDealsCategoryBinding
    private val dealsByCategoriesVM: DealsByCategoriesVM by viewModels()
    private lateinit var walletId: String
    private lateinit var balancedCoins: String
    private var selectedCategory: Int = -1

    private val offerAdapter: OfferAdapter by lazy {
        OfferAdapter(emptyList(), onItemClicked = { offer ->
            val intent = Intent(this@DealsByCategoryActivity, RedeemActivity::class.java).apply {
                putExtra("walletId", walletId)
                putExtra("balancedCoins", balancedCoins)
                putExtra("offerId", offer.offerId)
                putExtra("reqPoints", offer.reqPoints)
                putExtra("canRedeem", false)
            }
            redeemActivityLauncher.launch(intent)

        })
    }

    private val redeemActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isSuccess = result.data?.getBooleanExtra("REDEEM_SUCCESS", false) ?: false
                if (isSuccess) {
                    Timber.d("redeem successful!")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDealsCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            dealsByCategoriesVM.selectedCategoryId.collect {
                selectedCategory = it
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initActivity()

    }

    private fun initActivity() {
        balancedCoins = intent.getStringExtra("balancedCoins").toString()
        walletId = intent.getStringExtra("walletId").toString()

        if (selectedCategory == -1)
            selectedCategory = intent.getIntExtra("selectedCategory", 0)

        val categoryList: ArrayList<Category>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("categoryList", Category::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("categoryList")
            }

        val allCategory = Category(0, "", "All", "")
        val updatedCategoryList = arrayListOf(allCategory)
        updatedCategoryList.addAll(categoryList ?: arrayListOf())
        populateChips(updatedCategoryList, selectedCategory)

        binding.tvRewardPoints.text = balancedCoins
        dealsByCategoriesVM.getDealsCategoryWise(selectedCategory, walletId)
        subscribeEvents()
        setupToolBar()
        setupOnClickListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.apply {
            val orientation = resources.configuration.orientation
            val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
            val spacing = resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)
            val includeEdge = true
            rvDealsCategory.addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount,
                    spacing,
                    includeEdge
                )
            )
            rvDealsCategory.layoutManager =
                GridLayoutManager(this@DealsByCategoryActivity, spanCount)
            rvDealsCategory.adapter = offerAdapter
        }
    }

    private fun subscribeEvents() {
        lifecycleScope.launch {
            dealsByCategoriesVM.dealsByCategoriesFlow.collect {
                when (it) {
                    is DealsByCategoriesEvents.Error -> {
                        binding.pbDealsCategories.visibility = View.GONE
                        CustomToast.showErrorToast(this@DealsByCategoryActivity, it.message)
                    }

                    is DealsByCategoriesEvents.Loading -> {
                        binding.pbDealsCategories.visibility = View.VISIBLE
                    }

                    is DealsByCategoriesEvents.Success -> {
                        binding.pbDealsCategories.visibility = View.GONE
                        val offerDetailsList = it.response.offerDetailsList
                        if (offerDetailsList.isEmpty()) {
                            binding.tvNoOffers.visibility = View.VISIBLE
                            binding.rvDealsCategory.visibility = View.GONE
                        } else {
                            binding.tvNoOffers.visibility = View.GONE
                            binding.rvDealsCategory.visibility = View.VISIBLE
                        }
                        offerAdapter.updateOffers(offerDetailsList)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun populateChips(categoryList: ArrayList<Category>, selectedCategory: Int) {
        binding.chipGroup.removeAllViews()

        for (category in categoryList) {
            val chip = Chip(this).apply {
                id = category.catId
                text = category.catName
                isCheckable = true
                isClickable = true
                tag = category.catId

                chipBackgroundColor = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        ContextCompat.getColor(
                            this@DealsByCategoryActivity,
                            R.color.colorFrenchViolet
                        ),
                        ContextCompat.getColor(
                            this@DealsByCategoryActivity,
                            R.color.white
                        )
                    )
                )

                setTextColor(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            ContextCompat.getColor(
                                this@DealsByCategoryActivity,
                                R.color.white
                            ),
                            ContextCompat.getColor(
                                this@DealsByCategoryActivity,
                                R.color.black
                            )
                        )
                    )
                )

                checkedIcon = null

                setChipStrokeColorResource(R.color.colorGray)
                chipStrokeWidth = resources.getDimension(R.dimen._0_1dp)
                chipIconSize = resources.getDimension(com.intuit.sdp.R.dimen._11sdp)

                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(23f)
                    .build()

                chipStartPadding = resources.getDimension(com.intuit.sdp.R.dimen._10sdp)
                chipEndPadding = resources.getDimension(com.intuit.sdp.R.dimen._10sdp)
                Glide.with(this@DealsByCategoryActivity)
                    .load(category.smallLogoUrl)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            chipIcon = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })

                binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                    if (checkedIds.isNotEmpty()) {
                        val selectedChipId = checkedIds[0]
                        val selectedChip = group.findViewById<Chip>(selectedChipId)
                        if (selectedChip != null) {
                            val categoryId = selectedChip.tag as? Int ?: 0
                            dealsByCategoriesVM.getDealsCategoryWise(categoryId, walletId)
                        }
                    }
                }
            }

            binding.chipGroup.addView(chip)

            if (category.catId == selectedCategory) {
                chip.isChecked = true

                chip.postDelayed({
                    val scrollToX = chip.left
                    binding.hsChips.smoothScrollTo(scrollToX, 0)
                }, 100)
            }
        }
        binding.chipGroup.isSelectionRequired = true
    }

    private fun setupOnClickListeners() {
        binding.apply {
            myToolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
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
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.pointerCount == 1 && super.dispatchTouchEvent(ev)
    }
}