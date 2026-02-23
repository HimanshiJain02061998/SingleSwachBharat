package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.about_coins

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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.ActivityAboutCoinBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.AboutCoinDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AboutCoinsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutCoinBinding
    private val viewModel: AboutCoinsViewModel by viewModels()
    private lateinit var aboutCoinsAdapter: AboutCoinsAdapter
    private var aboutCoinsList: List<AboutCoinDetail> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAboutCoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
    }

    private fun initViews() {
        setupRecyclerView()
        setupToolBar()
        subscribeChannelEvents()
    }

    private fun setupRecyclerView() {

        aboutCoinsAdapter = AboutCoinsAdapter(aboutCoinsList)
        binding.rvAboutCoins.apply {
            layoutManager = LinearLayoutManager(this@AboutCoinsActivity)
            setHasFixedSize(true)
            adapter = aboutCoinsAdapter

            if (itemDecorationCount == 0) {
                addItemDecoration(
                    DividerItemDecoration(this@AboutCoinsActivity, RecyclerView.VERTICAL)
                )
            }
        }
    }

    private fun subscribeChannelEvents() {

        lifecycleScope.launch {
            viewModel.aboutCoinsList.collect {
                aboutCoinsList = it
                aboutCoinsAdapter.updateData(aboutCoinsList)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.aboutCoinsState.collect {
                    when (it) {
                        is AboutCoinsState.Error -> {
                            CustomToast.showErrorToast(this@AboutCoinsActivity, it.message)
                        }

                        is AboutCoinsState.Loading -> {

                            if (it.isLoading) {
                                binding.pbAboutCoins.visibility =
                                    View.VISIBLE
                            } else {
                                binding.pbAboutCoins.visibility = View.GONE
                            }
                        }

                        is AboutCoinsState.Success -> {
                            Timber.d(it.aboutCoinsResponse.message)
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