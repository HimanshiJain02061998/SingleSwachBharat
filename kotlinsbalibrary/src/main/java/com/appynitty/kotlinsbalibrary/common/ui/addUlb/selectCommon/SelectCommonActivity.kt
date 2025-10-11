package com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.response.ULBListItem
import com.appynitty.kotlinsbalibrary.common.ui.addCity.adapter.DistrictAdapter
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.AddUlbActivity
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.adapter.CommonDistUlbParentAdapter
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.adapter.DistUlbChildAdapter
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.CommonDistUlbModel
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.CommonDistUlbParentModel
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.model.DistCityType
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.selectCommon.viewModel.SelectCommonViewmodel
import com.appynitty.kotlinsbalibrary.common.ui.addUlb.viewModel.AddUlbViewModel
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityAddUlbBinding
import com.appynitty.kotlinsbalibrary.databinding.ActivitySelectCommonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class SelectCommonActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectCommonBinding
    private val viewModel: SelectCommonViewmodel by viewModels()
    private var isInternetOn = false
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private lateinit var languageDataStore: LanguageDataStore
    var parentAdapter: CommonDistUlbParentAdapter? = null
    var commonList: MutableList<CommonDistUlbParentModel>? = mutableListOf()
    var typeCity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        initToolbar()
        setOnClickListner()
        subscribeLiveData()
        subscribeChannelEvent()

    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun init() {
        typeCity = intent.getStringExtra(DistCityType.Type.type) ?: ""
        val distId = intent.getIntExtra(DistCityType.Id.type, -1)
        if (typeCity==DistCityType.Dist.type){
            viewModel.getDistrictList()
            binding.toolbar.title = resources.getString(R.string.select_district)
        }else{
            viewModel.getUlbList(distId)
            binding.toolbar.title = resources.getString(R.string.select_ulb)
        }


        binding.etCityName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterPlace(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        parentAdapter = CommonDistUlbParentAdapter(this, commonList)
        binding.rvParent?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvParent?.adapter = parentAdapter



        DistUlbChildAdapter.setAdapterAListener(object :DistUlbChildAdapter.DistUlbChildAdapterListener{
            override fun onItemClick(commonDistUlbModel: CommonDistUlbModel?) {

                val resultIntent = Intent().apply {
                    putExtra(DistCityType.Type.type, typeCity)
                    putExtra(DistCityType.Name.type, commonDistUlbModel?.name)
                    putExtra(DistCityType.Id.type, commonDistUlbModel?.id)
                }
                setResult(RESULT_OK, resultIntent)
                finish()

            }
        })

    }

    private fun setOnClickListner() {

    }

    private fun filterPlace(text: String){
        val lowerQuery = text.lowercase(Locale.getDefault())

        val filteredList = commonList?.map { parent ->
            val filteredChildren = parent.list.filter { city ->
                city.name.lowercase(Locale.getDefault()).contains(lowerQuery)
            }
            parent.copy(list = filteredChildren)
        }?.filter { it.list.isNotEmpty() } // remove groups with no matches

        parentAdapter?.updateList(filteredList ?: emptyList())
    }

    private fun subscribeLiveData() {
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
        }

    }


    private fun subscribeChannelEvent() {

        lifecycleScope.launch {
            viewModel.selectCommonEventsFlow.collect { event ->
                when (event) {
                    SelectCommonViewmodel.SelectCommonEvent.HideProgressBar -> hideProgressBar()
                    SelectCommonViewmodel.SelectCommonEvent.NavigateToLogin -> {

                    }

                    is SelectCommonViewmodel.SelectCommonEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@SelectCommonActivity, event.msg)
                    }

                    SelectCommonViewmodel.SelectCommonEvent.ShowProgressBar -> showProgressBar()
                    is SelectCommonViewmodel.SelectCommonEvent.ShowResponseErrorMessage -> showApiErrorMessage(
                        event.msg,
                        event.msgMr
                    )

                    is SelectCommonViewmodel.SelectCommonEvent.ShowResponseSuccessMessage -> showApiErrorMessage(
                        event.msg,
                        event.msgMr
                    )

                    is SelectCommonViewmodel.SelectCommonEvent.DistrictListResponse -> {
                       event.districtList

                      val tempLsit =  event.districtList
                            ?.filterNotNull()
                            ?.map { item ->
                                CommonDistUlbModel(item.districtName ?: "", item.disid ?: 0)
                            }
                            ?.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
                            ?.map { (letter, cities) -> CommonDistUlbParentModel(letter.toString(), cities) }
                            ?: emptyList()

                        commonList = tempLsit as MutableList<CommonDistUlbParentModel>?
                        parentAdapter?.updateList(
                            tempLsit
                        )

                    }

                    is SelectCommonViewmodel.SelectCommonEvent.UlbListResponse -> {
                        val tempLsit =  event.uLbList
                            ?.filterNotNull()
                            ?.map { item ->
                                CommonDistUlbModel(item.uLBName ?: "", item.appid ?: 0)
                            }
                            ?.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
                            ?.map { (letter, cities) -> CommonDistUlbParentModel(letter.toString(), cities) }
                            ?: emptyList()

                        commonList = tempLsit as MutableList<CommonDistUlbParentModel>?
                        parentAdapter?.updateList(
                            tempLsit
                        )
                    }

                    is SelectCommonViewmodel.SelectCommonEvent.ShowSuccessMessage -> {
                        CustomToast.showSuccessToast(
                            this@SelectCommonActivity, resources.getString(event.resourceId)
                        )
                    }

                    is SelectCommonViewmodel.SelectCommonEvent.ShowFailureMessageRes -> {
                        CustomToast.showErrorToast(
                            this@SelectCommonActivity, resources.getString(event.resourceId)
                        )
                    }

                }
            }
        }

    }

    private fun showApiErrorMessage(msg: String?, msgMr: String?) {
        if (selectedLanguageId == "mr") {
            msgMr?.let {
                CustomToast.showErrorToast(this, msgMr)
            }
        } else {
            msg?.let {
                CustomToast.showErrorToast(this, msg)
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun attachBaseContext(newBase: Context?) {

        newBase?.let { context ->
            languageDataStore = LanguageDataStore(newBase.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            selectedLanguage = appLanguage.languageName
            selectedLanguageId = appLanguage.languageId
            LanguageConfig.changeLanguage(context, selectedLanguageId.toString())
        }
        super.attachBaseContext(newBase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) return true
        return super.dispatchTouchEvent(ev)
    }

}