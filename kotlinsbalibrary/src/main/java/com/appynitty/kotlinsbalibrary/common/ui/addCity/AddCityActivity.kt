package com.appynitty.kotlinsbalibrary.common.ui.addCity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.model.response.DistrictListItem
import com.appynitty.kotlinsbalibrary.common.model.response.ULBListItem
import com.appynitty.kotlinsbalibrary.common.ui.addCity.adapter.DistrictAdapter
import com.appynitty.kotlinsbalibrary.common.ui.addCity.adapter.UlbAdapter
import com.appynitty.kotlinsbalibrary.common.ui.addCity.viewModel.AddCityViewModel
import com.appynitty.kotlinsbalibrary.common.ui.login.LoginActivity
import com.appynitty.kotlinsbalibrary.common.utils.ConnectivityStatus
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityAddCityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class AddCityActivity : AppCompatActivity() {

    private val viewModel: AddCityViewModel by viewModels()
    private lateinit var binding: ActivityAddCityBinding
    private var isInternetOn = false
    private var selectedLanguage: String? = null
    private var selectedLanguageId: String? = null
    private lateinit var languageDataStore: LanguageDataStore
    lateinit var districtAdapter: DistrictAdapter
    lateinit var ulbAdapter: UlbAdapter
    var district: DistrictListItem? = null
    var ulb: ULBListItem? = null

    var ulbList: List<ULBListItem?>? = null
    var districtList: List<DistrictListItem?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rlUlb.isEnabled = false
        init()
        subscribeLiveData()
        setOnClickListner()
        subscribeChannelEvent()
    }

    private fun init() {
        viewModel.getDistrictList()
    }

    private fun setOnClickListner() {
        binding.rlDist.setOnClickListener {
            showDistrictDialog(binding.tvDist, districtList)
        }
        binding.rlUlb.setOnClickListener {
                showCityDialog(binding.tvUlb, ulbList)
        }
        binding.btnGoAhead.setOnClickListener {
            if (viewModel.validateUlb(
                    binding.tvDist.text.toString().trim(),
                    binding.tvUlb.text.toString().trim()
                )
            ) {
                viewModel.selectUlb(ulb?.appid.toString(), ulb?.uLBName)
            }
        }
    }

    private fun subscribeLiveData() {
        ConnectivityStatus(this).observe(this) {
            isInternetOn = it
        }

    }


    private fun subscribeChannelEvent() {

        lifecycleScope.launch {
            viewModel.addCityEventsFlow.collect { event ->
                when (event) {
                    AddCityViewModel.AddCityEvent.HideProgressBar -> hideProgressBar()
                    AddCityViewModel.AddCityEvent.NavigateToLogin -> {
                        startActivity(Intent(this@AddCityActivity, LoginActivity::class.java))
                        finish()
                    }

                    is AddCityViewModel.AddCityEvent.ShowFailureMessage -> {
                        CustomToast.showErrorToast(this@AddCityActivity, event.msg)
                    }

                    AddCityViewModel.AddCityEvent.ShowProgressBar -> showProgressBar()
                    is AddCityViewModel.AddCityEvent.ShowResponseErrorMessage -> showApiErrorMessage(
                        event.msg,
                        event.msgMr
                    )

                    is AddCityViewModel.AddCityEvent.ShowResponseSuccessMessage -> showApiErrorMessage(
                        event.msg,
                        event.msgMr
                    )

                    is AddCityViewModel.AddCityEvent.DistrictListResponse -> {
                        districtList = event.districtList
                        Log.d("districtlist", "list is ${event.districtList}")
                    }

                    is AddCityViewModel.AddCityEvent.UlbListResponse -> {
                        ulbList = event.uLbList
                    }

                    is AddCityViewModel.AddCityEvent.ShowSuccessMessage -> {
                        CustomToast.showSuccessToast(
                            this@AddCityActivity, resources.getString(event.resourceId)
                        )
                    }

                    is AddCityViewModel.AddCityEvent.ShowFailureMessageRes -> {
                        CustomToast.showErrorToast(
                            this@AddCityActivity, resources.getString(event.resourceId)
                        )
                    }
                }
            }
        }

    }

    private fun showDistrictDialog(spCity: AppCompatTextView, data: List<DistrictListItem?>?) {
//        val dialog = Dialog(requireContext(), R.style.MyAlertDialogTheme)
        val dialog = Dialog(this)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        // val height = (resources.displayMetrics.heightPixels * 0.90).toInt()

        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(R.layout.dialog_city_spinner)
        val tvTitle = dialog.findViewById<AppCompatTextView>(R.id.tvTitle)
        tvTitle.text = getString(R.string.select_district)
        val rvCity = dialog.findViewById<RecyclerView>(R.id.rvCity)
        val etCityName = dialog.findViewById<EditText>(R.id.etCityName)
        val ll3 = dialog.findViewById<LinearLayout>(R.id.ll3)

        etCityName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterDistrict(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        districtAdapter = DistrictAdapter(this, data)
        rvCity?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCity?.adapter = districtAdapter

        districtAdapter.setAdapterAListener(object : DistrictAdapter.DistrictAdapterListener {
            override fun onItemClick(name: DistrictListItem?) {
                binding.rlUlb.isEnabled = true
                district = name
                spCity.setText(name?.districtName)
                dialog.dismiss()
                binding.tvUlb.text = ""
                ulb = null
                name?.disid?.toInt()?.let { viewModel.getUlbList(it) }
            }

        })

        ll3!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCityDialog(spCity: AppCompatTextView, stateList: List<ULBListItem?>?) {
        val dialog = Dialog(this)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()

        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(R.layout.dialog_city_spinner)
        val tvTitle = dialog.findViewById<AppCompatTextView>(R.id.tvTitle)
        tvTitle.text = getString(R.string.select_ulb)
        val rvCity = dialog.findViewById<RecyclerView>(R.id.rvCity)
        val etCityName = dialog.findViewById<EditText>(R.id.etCityName)
        val ll3 = dialog.findViewById<LinearLayout>(R.id.ll3)



        etCityName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterUlb(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        ulbAdapter = UlbAdapter(this, stateList)
        rvCity?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCity?.adapter = ulbAdapter

        ulbAdapter.setAdapterAListener(object : UlbAdapter.UlbAdapterListener {


            override fun onItemClick(name: ULBListItem?) {
                ulb = name
                spCity.setText(name?.uLBName)
                dialog.dismiss()
            }

        })


        ll3!!.setOnClickListener {

            dialog.dismiss()
        }


        dialog.show()
    }


    private fun filterDistrict(text: String) {
        val filteredlist: ArrayList<DistrictListItem?>? = ArrayList<DistrictListItem?>()
        if (districtList != null) {
            for (item in districtList!!) {
                if (item?.districtName?.lowercase()
                        ?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    filteredlist?.add(item)
                }
            }
        }

        if (filteredlist?.isEmpty() == true) {
            /*  CustomToast.showErrorToast(
                  this@AddCityActivity, resources.getString(R.string.no_archived_data_error)
              )*/
        } else {
            districtAdapter.filterList(filteredlist)
        }
    }

    private fun filterUlb(text: String) {
        val filteredlist: ArrayList<ULBListItem?>? = ArrayList<ULBListItem?>()
        if (ulbList != null) {
            for (item in ulbList!!) {
                if (item?.uLBName?.lowercase()
                        ?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    filteredlist?.add(item)
                }
            }
        }

        if (filteredlist?.isEmpty() == true) {
            /*    CustomToast.showErrorToast(
                    this@AddCityActivity, resources.getString(R.string.no_archived_data_error)
                )*/
        } else {
            ulbAdapter.filterList(filteredlist)
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
        binding.loginProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.loginProgressBar.visibility = View.GONE
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Only process single-touch events
        if (ev.pointerCount > 1) return true
        return super.dispatchTouchEvent(ev)
    }

}