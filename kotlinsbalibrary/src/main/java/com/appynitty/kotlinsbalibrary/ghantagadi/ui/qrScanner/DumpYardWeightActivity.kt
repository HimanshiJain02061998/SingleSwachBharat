package com.appynitty.kotlinsbalibrary.ghantagadi.ui.qrScanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraActivity
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils.IMAGE_PATH
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.databinding.ActivityDumpYardWeightBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class DumpYardWeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDumpYardWeightBinding
    private lateinit var referenceId: String
    private var latitude: String? = null
    private var longitude: String? = null
    private lateinit var userDataStore: UserDataStore
    private var dryImageFilePath: String? = null
    private var wetImageFilePath: String? = null


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

        if (savedInstanceState != null) {
            finish()
        }
        binding = ActivityDumpYardWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        initToolbar()
        registerClickEvents()
        subscribeLiveData()
        BackBtnPressedUtil.handleBackBtnPressed(
            this, this, this
        )
    }

    private fun subscribeLiveData() {

        userDataStore.getUserLatLong.asLiveData().observe(this) {
            latitude = it.latitude
            longitude = it.longitude
        }
    }

    private fun calculateWight(): String {

        val wetWeight = binding.txtWetWeight.text.toString()
        val dryWeight = binding.txtDryWeight.text.toString()

        if (wetWeight.isNotEmpty() || dryWeight.isNotEmpty()) {

            if ((wetWeight.length == 1 && wetWeight == ".") || (dryWeight.length == 1 && dryWeight == ".")) {
                CustomToast.showWarningToast(
                    this, resources.getString(R.string.please_type_valid_weight)
                )
            } else {

                var totalWeight = 0.0

                if (wetWeight.isNotEmpty() && dryWeight.isEmpty()) {
                    try {
                        totalWeight = wetWeight.toDouble()
                    } catch (e: Exception) {
                        CustomToast.showWarningToast(
                            this, resources.getString(R.string.please_type_valid_weight)
                        )
                    }
                } else if (dryWeight.isNotEmpty() && wetWeight.isEmpty()) {
                    try {
                        totalWeight = dryWeight.toDouble()
                    } catch (e: Exception) {
                        CustomToast.showWarningToast(
                            this, resources.getString(R.string.please_type_valid_weight)
                        )
                    }
                } else {
                    try {
                        totalWeight = wetWeight.toDouble() + dryWeight.toDouble()
                    } catch (e: Exception) {
                        CustomToast.showWarningToast(
                            this, resources.getString(R.string.please_type_valid_weight)
                        )
                    }
                }

                return totalWeight.toString()
            }
        }
        return "0.00"
    }

    private fun initVars() {

        referenceId = intent.getStringExtra("REFERENCE_ID").toString()
        binding.txtHouseId.text = referenceId
        binding.txtTotalWeight.text = "0.0"
        userDataStore = UserDataStore(applicationContext)

        if (!referenceId.substring(0, 2).matches("^[DdYy]+\$".toRegex())){
            binding.placeholder.text = resources.getString(R.string.dump_yard_vehicle_id_txt)
        }
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_dump_yard_weight)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun registerClickEvents() {
        binding.btnTakeDryPhoto.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(this, 1))
        }

        binding.btnTakeWetPhoto.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(this, 2))

        }
        binding.btnSubmitDump.setOnClickListener {

            val dryWeight = binding.txtDryWeight.text.toString()
            val wetWeight = binding.txtWetWeight.text.toString()
            val totalWeight = binding.txtTotalWeight.text.toString()

            if (totalWeight.toDouble() <= 0.0) {
                CustomToast.showWarningToast(
                    this, resources.getString(R.string.empty_dump_weight_error)
                )

            } else if (dryWeight.isNotEmpty() || wetWeight.isNotEmpty()) {

                val intent = Intent()

                if (dryWeight.isNotEmpty()) {
                    intent.putExtra("dryWeight", (dryWeight.toDouble() / 1000).toString())
                }
                if (wetWeight.isNotEmpty()) {
                    intent.putExtra("wetWeight", (wetWeight.toDouble() / 1000).toString())
                }

                intent.putExtra("totalWeight", (totalWeight.toDouble() / 1000).toString())
                intent.putExtra("referenceId", referenceId)
                intent.putExtra("dryImageFilePath", dryImageFilePath)
                intent.putExtra("wetImageFilePath", wetImageFilePath)

                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }

        binding.txtWetWeight.addTextChangedListener {
            binding.txtTotalWeight.text = calculateWight()
        }
        binding.txtDryWeight.addTextChangedListener {
            binding.txtTotalWeight.text = calculateWight()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(
                this
            )
        }
        return super.onOptionsItemSelected(item)
    }


    private val openCameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val filePath = result.data?.getStringExtra(IMAGE_PATH).toString()
                val requestCode: Int? = result.data?.getIntExtra(CameraActivity.REQUEST_ID, 0)


                val bitmap = BitmapFactory.decodeFile(filePath)

                if (requestCode == 1) {
                    binding.btnTakeDryPhoto.setImageBitmap(bitmap)
                    dryImageFilePath = filePath

                } else if (requestCode == 2) {
                    binding.btnTakeWetPhoto.setImageBitmap(bitmap)
                    wetImageFilePath = filePath
                }

            }
        }

    override fun finish() {

        overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
        super.finish()
    }
}