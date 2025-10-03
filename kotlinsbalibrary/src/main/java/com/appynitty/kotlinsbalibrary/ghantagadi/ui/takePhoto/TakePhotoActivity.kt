package com.appynitty.kotlinsbalibrary.ghantagadi.ui.takePhoto

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraActivity
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.SessionDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.UserDataStore
import com.appynitty.kotlinsbalibrary.databinding.ActivityTakePhotoBinding
import com.appynitty.kotlinsbalibrary.ghantagadi.ui.qrScanner.QRScannerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TakePhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakePhotoBinding
    private var empType: String? = null
    private var userId: String? = null
    private var vehicleNumber: String? = null
    private var selectedLanguageId: String? = null
    private var userTypeId: String? = null
    private var beforeImageFilePath: String? = null
    private var afterImageFilePath: String? = null
    private var isGtFeatureOn: Boolean = true

    @Inject
    lateinit var sessionDataStore: SessionDataStore
    @Inject
    lateinit var userDataStore : UserDataStore

    //multi language functionality
    override fun attachBaseContext(newBase: Context?) {

        var context: Context? = newBase
        if (newBase != null) {
            val languageDataStore = LanguageDataStore(newBase.applicationContext)

            //getting language data from datastore
            val appLanguage = languageDataStore.currentLanguage
            context = newBase.let { LanguageConfig.changeLanguage(it, appLanguage.languageId) }

        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initVars()
        initToolbar()
        clickEvents()
        BackBtnPressedUtil.handleBackBtnPressed(
            this,
            this,
            this
        )

    }


    private fun initVars() {

        empType = intent.getStringExtra("empType")
        selectedLanguageId = intent.getStringExtra("languageId")
        userId = intent.getStringExtra("userId")
        vehicleNumber = intent.getStringExtra("vehicleNumber")
        userTypeId = intent.getStringExtra("userTypeId")


        lifecycleScope.launch(Dispatchers.IO) {
            beforeImageFilePath = sessionDataStore.getBeforeImageFilePath.first()
            isGtFeatureOn = userDataStore.getIsBifurcationOn.first()
            withContext(Dispatchers.Main) {
                if (beforeImageFilePath != "") {

                    val bitmap = BitmapFactory.decodeFile(beforeImageFilePath)
                    binding.beforeIv.setImageBitmap(bitmap)
                    binding.beforeIv.scaleType = ImageView.ScaleType.FIT_XY

                }
            }
        }

    }

    private fun clickEvents() {

        binding.beforeIv.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(this, 1))
        }

        binding.afterIv.setOnClickListener {
            openCameraActivity.launch(CameraActivity.getIntent(this, 2))
        }

        binding.scannerCard.setOnClickListener {

            if ((beforeImageFilePath != null && beforeImageFilePath != "") || afterImageFilePath != null) {

                val intent = Intent(this, QRScannerActivity::class.java)

                intent.putExtra("empType", empType)
                intent.putExtra("languageId", selectedLanguageId)
                intent.putExtra("userId", userId)
                intent.putExtra("userTypeId", userTypeId)
                intent.putExtra("vehicleNumber", vehicleNumber)
                intent.putExtra("beforeImagePath", beforeImageFilePath)
                intent.putExtra("afterImagePath", afterImageFilePath)
                intent.putExtra("comment", binding.commentEt.text.toString())
                intent.putExtra("isGtFeatureOn", isGtFeatureOn)

                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_in_right, R.anim.slide_out_left
                )
                finish()

            } else {
                CustomToast.showWarningToast(this, resources.getString(R.string.plz_capture_img))
            }

        }
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.take_a_photo)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private val openCameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val filePath = result.data?.getStringExtra(CameraUtils.IMAGE_PATH).toString()
                val requestCode: Int? = result.data?.getIntExtra(CameraActivity.REQUEST_ID, 0)


                val bitmap = BitmapFactory.decodeFile(filePath)

                if (requestCode == 1) {
                    binding.beforeIv.setImageBitmap(bitmap)
                    binding.beforeIv.scaleType = ImageView.ScaleType.FIT_XY
                    beforeImageFilePath = filePath

                    lifecycleScope.launch(Dispatchers.IO) {
                        sessionDataStore.saveBeforeImageFilePath(filePath)
                    }

                } else if (requestCode == 2) {
                    binding.afterIv.setImageBitmap(bitmap)
                    binding.afterIv.scaleType = ImageView.ScaleType.FIT_XY

                    afterImageFilePath = filePath
                }

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
}