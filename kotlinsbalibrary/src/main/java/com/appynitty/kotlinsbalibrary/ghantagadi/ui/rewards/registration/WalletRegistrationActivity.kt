package com.appynitty.kotlinsbalibrary.ghantagadi.ui.rewards.registration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.response.UserDetailsResponse
import com.appynitty.kotlinsbalibrary.common.ui.userDetails.viewmodel.UserDetailsViewModel
import com.appynitty.kotlinsbalibrary.common.utils.CommonUtils
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.retrofit.ApiResponseListener
import com.appynitty.kotlinsbalibrary.databinding.ActivityWalletRegistrationBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.model.request.RewardsRegRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WalletRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalletRegistrationBinding
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()
    private val rewardsEmpRegVM: RewardsEmpRegVM by viewModels()
    private var userLoginId: String? = null

    @Inject
    lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWalletRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolBar()
        initComponents()
    }

    private fun initComponents() {
        lifecycleScope.launch { userLoginId = userDataStore.getUserEssentials.first().userLoginId }
        getUserDetailsFromApi()
        setupClickListeners()
        subscribeLiveData()
    }

    private fun subscribeLiveData() {
        userDetailsViewModel.userDetailsLiveData.observe(this) {
            when (it) {
                is ApiResponseListener.Failure -> {
                    binding.pbRegistration.visibility = View.GONE
                    Timber.d(it.message)
                }

                is ApiResponseListener.Loading -> {
                    binding.pbRegistration.visibility = View.VISIBLE
                }

                is ApiResponseListener.Success -> {
                    binding.pbRegistration.visibility = View.GONE
                    autoFillTheFields(it.data)
                }
            }
        }

        rewardsEmpRegVM.empRegistrationLiveData.observe(this) {
            when (it) {
                is ApiResponseListener.Failure -> {
                    binding.pbRegistration.visibility = View.GONE
                    CustomToast.showErrorToast(
                        this@WalletRegistrationActivity,
                        it.message.toString()
                    )
                }

                is ApiResponseListener.Loading -> {
                    binding.pbRegistration.visibility = View.VISIBLE
                }

                is ApiResponseListener.Success -> {
                    binding.pbRegistration.visibility = View.GONE
                    CustomToast.showSuccessToast(
                        this@WalletRegistrationActivity,
                        it.data?.message.toString()
                    )
                    Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3000)
                }
            }
        }
    }

    private fun autoFillTheFields(userData: UserDetailsResponse?) {
        val name = userData?.name
        val (firstName, middleName, lastName) = splitFullName(name.toString())
        val mobile = userData?.mobileNumber
        val address = userData?.address
        binding.etFirstName.setText(firstName)
        binding.etMiddleName.setText(middleName)
        binding.etLastName.setText(lastName)
        binding.etMobileNo.setText(mobile)
        binding.etAddress.setText(address)
    }

    private fun getUserDetailsFromApi() {

        lifecycleScope.launch {
            val userEssentials = userDataStore.getUserEssentials.first()
            userDetailsViewModel.getUserDetails(
                CommonUtils.APP_ID,
                CommonUtils.CONTENT_TYPE,
                userEssentials.userId,
                userEssentials.userTypeId,
                userEssentials.employeeType
            )
        }
    }

    private fun splitFullName(fullName: String): Triple<String, String, String> {
        val parts = fullName.trim().split(" ")

        val firstName = parts.getOrNull(0) ?: ""
        val middleName =
            if (parts.size > 2) parts.subList(1, parts.size - 1).joinToString(" ") else ""
        val lastName = if (parts.size > 1) parts.last() else ""

        return Triple(firstName, middleName, lastName)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (isValidInput()) {
                rewardsEmpRegVM.registerEmployee(getRewardsRegDTO())
            }
        }

        binding.etAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etAddress.windowToken, 0)
                binding.etAddress.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun getRewardsRegDTO() = RewardsRegRequest(
        appId = CommonUtils.APP_ID.toInt(),
        EUsername = userLoginId.toString(),
        firstName = binding.etFirstName.text.toString(),
        middleName = binding.etMiddleName.text.toString(),
        lastName = binding.etLastName.text.toString(),
        mobileNo = binding.etMobileNo.text.toString(),
        address = binding.etAddress.text.toString(),
        emailId = binding.etEmailId.text.toString()
    )

    private fun isValidInput(): Boolean {
        val fName = binding.etFirstName.text.toString()
        val mName = binding.etMiddleName.text.toString()
        val lName = binding.etLastName.text.toString()
        val mobileNo = binding.etMobileNo.text.toString()
        val address = binding.etAddress.text.toString()
        val mobRegex = "^(\\+91|91)?[6-9]\\d{9}$"

        if (fName.isEmpty()) {
            binding.etFirstName.error = "Please enter first name"
            return false
        } else if (mName.isEmpty()) {
            binding.etMiddleName.error = "Please enter middle name"
            return false
        } else if (lName.isEmpty()) {
            binding.etLastName.error = "Please enter last name"
            return false
        } else if (mobileNo.isEmpty() || !mobileNo.matches(mobRegex.toRegex())) {
            binding.etMobileNo.error = "Please enter a valid mobile number"
            return false
        } else if (address.isEmpty()) {
            binding.etAddress.error = "Please enter address"
            return false
        } else {
            return true
        }
    }

    private fun setupToolBar() {
        setSupportActionBar(binding.myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawable = binding.myToolbar.navigationIcon
        drawable?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(this, R.color.white))
            binding.myToolbar.navigationIcon = it
        }

        binding.myToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}