package com.appynitty.kotlinsbalibrary.common.ui.profile

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.UserData
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userData: UserData


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

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()

        userData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("userData", UserData::class.java)!!
        } else {

            @Suppress("DEPRECATION")
            intent.extras?.getParcelable("userData")!!
        }
        setUserData()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)
    }

    private fun setUserData() {
        if (userData.userProfileImage?.isNotEmpty() == true) {
            Glide.with(this).load(userData.userProfileImage)
                .into(binding.profileProfilePic)
        }

        binding.profileEmpId.text = userData.employeeId
        binding.profileUserName.text = userData.userName
        binding.profileContactNo.text = userData.userMobileNo
        binding.profileBloodGroup.text = userData.userBloodGroup
        binding.profileAddress.text = userData.userAddress


        when (userData.employeeType) {
            "N" -> {
                binding.profileEmployeeType.text = resources.getString(R.string.household_collection)
            }
            "S" -> {
                binding.profileEmployeeType.text = resources.getString(R.string.street_sweeping)
            }
            "L" -> {
                binding.profileEmployeeType.text = resources.getString(R.string.liquid_waste_cleaning)
            }
            "D" -> {
                binding.profileEmployeeType.text = resources.getString(R.string.dump_yard_supervisor)

            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_profile_page)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)
        }
        return super.onOptionsItemSelected(item)
    }


}