package com.appynitty.kotlinsbalibrary.common.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.ui.camera.CameraUtils.IMAGE_PATH
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.common.utils.DateTimeUtils.Companion.getFileNameTimeStamp
import com.appynitty.kotlinsbalibrary.databinding.ActivityCameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.swapy.imagecompressor.ImageCompressor

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService

    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    binding.root,
                    "The camera permission is necessary",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            finish()
        }

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        cameraPermissionResult.launch(android.Manifest.permission.CAMERA)

        binding.imgCaptureBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            takePhoto()
            animateFlash()
            binding.imgCaptureBtn.visibility = View.GONE
            binding.transparentWhiteBg.visibility = View.VISIBLE
        }
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = getFileNameTimeStamp() + ".jpeg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.e(TAG, "onImageSaved: ${file.absolutePath}")
                        val compressedImagePath = ImageCompressor.compressImage(file.absolutePath)

                        val intent = Intent().apply {
                            putExtra(IMAGE_PATH, compressedImagePath)
                            putExtra(REQUEST_ID, intent.getIntExtra(REQUEST_ID, 0))
                        }

                        runOnUiThread {
                            CustomToast.showSuccessToast(
                                this@CameraActivity,
                                resources.getString(R.string.photo_clicked)
                            )
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                    override fun onError(exception: ImageCaptureException) {

                        runOnUiThread {
                            Toast.makeText(
                                this@CameraActivity,
                                "Error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.imgCaptureBtn.visibility = View.VISIBLE

                        }
                    }
                })
        }
    }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val REQUEST_ID = "REQUEST_ID"

        fun getIntent(context: Context, postId: Int): Intent {
            return Intent(context, CameraActivity::class.java).apply {
                putExtra(REQUEST_ID, postId)
            }
        }
    }

    override fun finish() {

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        super.finish()
    }
}