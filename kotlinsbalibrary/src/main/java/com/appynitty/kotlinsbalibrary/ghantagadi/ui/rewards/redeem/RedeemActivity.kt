package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.redeem

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.ActivityRedeemBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RedeemOfferRequest
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.DailyDealDetail
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.OfferDetails
import com.appynitty.kotlinsbalibrary.ghantagadi.model.response.RedeemedOfferResponse
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class RedeemActivity : AppCompatActivity(), ConfirmButtonListener {
    private lateinit var binding: ActivityRedeemBinding
    private val viewmodel: RedeemOfferVM by viewModels()
    private var walletId: String? = null
    private var balancedCoins: String? = null
    private var offerId: Int? = null
    private var reqPoints: Int? = null
    private var redeemedOfferResponse: RedeemedOfferResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRedeemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    private fun initView() {
        val dailyDealsDetails =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("dailyDealDetail", DailyDealDetail::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("dailyDealDetail")
            }

        offerId = dailyDealsDetails?.offerId ?: intent.getIntExtra("offerId", 0)
        reqPoints = dailyDealsDetails?.reqPoints ?: intent.getIntExtra("reqPoints", 0)
        walletId = intent.getStringExtra("walletId")
        val isRedeemed =
            dailyDealsDetails?.isOfferRedeemed ?: intent.getBooleanExtra("canRedeem", false)
        balancedCoins = intent.getStringExtra("balancedCoins")

        Timber.d("initView: offerId: $offerId, reqPoints: $reqPoints, walletId: $walletId, balancedCoins: $balancedCoins")
        setupToolBar()
        binding.tvRewardPoints.text = balancedCoins
        binding.myToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (isRedeemed) {
            binding.tvAvail.visibility = View.GONE
        } else {
            binding.tvAvail.visibility = View.VISIBLE
        }


        offerId?.let { viewmodel.getOfferDetails(walletId.toString(), it) }

        subscribeRedeemEvents()

        binding.tvAvail.setOnClickListener {
            if (balancedCoins != null) {
                showConfirmationSheet(reqPoints ?: 0)
            }
        }
    }

    private fun subscribeRedeemEvents() {
        lifecycleScope.launch {
            viewmodel.redeemOfferEventsFlow.collect { event ->
                when (event) {
                    RedeemActivityEvents.HideProgressBar -> {
                        binding.pbRedeemActivity.visibility = View.GONE
                    }

                    is RedeemActivityEvents.OnSuccess -> {
                        val offerDetails = event.data as? OfferDetails
                        redeemedOfferResponse = event.data as? RedeemedOfferResponse
                        if (offerDetails != null) {
                            Timber.d("OnSuccess: " + event.data.offerData)
                            val offerData = event.data.offerData[0]
                            val isOfferRedeemed = offerData.offerRedeem

                            if (isOfferRedeemed) {
                                binding.tvAvail.visibility = View.GONE
                            }

                            Glide.with(this@RedeemActivity)
                                .load(offerData.offerImgUrl)
                                .placeholder(R.drawable.img_zomato)
                                .into(binding.ivOfferImage)


                            binding.tvOfferTxt.text = offerData.offerTitle
                            binding.tvValidTill.text = formatDate(offerData.offerEndTime)

                            val howToRedeem = offerData.offerDesc.trimIndent()
                            binding.tvHowToRedeem.text =
                                Html.fromHtml(howToRedeem, HtmlCompat.FROM_HTML_MODE_LEGACY)

                            val termsAndConditions = offerData.offerTerms.trimIndent()
                            binding.tvTermsAndConditions.text =
                                Html.fromHtml(termsAndConditions, HtmlCompat.FROM_HTML_MODE_LEGACY)

                            val reqPoints = offerData.reqPoints
                            binding.tvAvail.text = getString(R.string.avail_for, reqPoints)
                        } else if (redeemedOfferResponse != null) {
                            balancedCoins = balancedCoins?.toInt()?.minus(reqPoints!!).toString()
                            binding.tvRewardPoints.text = balancedCoins
                            Timber.d("on redeem: balanced coins: $balancedCoins")
                            val bundle = Bundle()
                            bundle.putParcelable(
                                "redeemResponse",
                                redeemedOfferResponse!!.redeemOfferData
                            )

                            val redeemSuccessBottomSheet = RedeemSuccessBottomSheet()
                            redeemSuccessBottomSheet.arguments = bundle
                            supportFragmentManager.let {
                                redeemSuccessBottomSheet.show(
                                    it,
                                    RedeemSuccessBottomSheet.TAG
                                )
                            }
                        }
                    }

                    is RedeemActivityEvents.ShowFailureMessage -> {
                        Timber.e("onFailure: " + event.msg)
                        CustomToast.showErrorToast(this@RedeemActivity, event.msg)
                    }

                    RedeemActivityEvents.ShowProgressBar -> {
                        binding.pbRedeemActivity.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showConfirmationSheet(amount: Int) {
        val bottomSheetDialog = AvailBottomSheetDialog.newInstance(amount)
        bottomSheetDialog.show(supportFragmentManager, "AvailBottomSheetDialog")
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }

    fun onBottomSheetDismissed() {
        val resultIntent = Intent().apply {
            putExtra("REDEEM_SUCCESS", true)
            putExtra("couponCode", redeemedOfferResponse?.redeemOfferData?.couponCode)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
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

    override fun onConfirmButtonClicked(reqPoints: Int) {
        walletId?.let { walletId ->
            offerId?.let { offerId ->
                val reqBody = RedeemOfferRequest(walletId, offerId)
                viewmodel.redeemOffer(reqBody)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.pointerCount == 1 && super.dispatchTouchEvent(ev)
    }
}