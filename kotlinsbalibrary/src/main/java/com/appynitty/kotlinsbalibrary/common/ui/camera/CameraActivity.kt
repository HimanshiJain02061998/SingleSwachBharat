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
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null

    private lateinit var imgCaptureExecutor: ExecutorService

    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permitted ->
            if (permitted) startCamera()
            else Snackbar.make(
                binding.root,
                "Camera permission required",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private fun startCamera() {

        preview = Preview.Builder().build().also { p ->
            p.setSurfaceProvider(binding.preview.surfaceProvider)
        }

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val provider = cameraProvider ?: return@addListener

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding error: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return

        val file = File(externalMediaDirs.first(), getFileNameTimeStamp() + ".jpeg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        capture.takePicture(
            outputOptions, imgCaptureExecutor,
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    val compressedPath = ImageCompressor.compressImage(file.absolutePath)

                    val outputIntent = Intent().apply {
                        putExtra(IMAGE_PATH, compressedPath)
                        putExtra(REQUEST_ID, intent.getIntExtra(REQUEST_ID, 0))
                    }

                    runOnUiThread {
                        CustomToast.showSuccessToast(
                            this@CameraActivity,
                            getString(R.string.photo_clicked)
                        )
                    }

                    setResult(Activity.RESULT_OK, outputIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            "Error capturing photo",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.imgCaptureBtn.visibility = View.VISIBLE
                    }
                }
            }
        )
    }

    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    // IMPORTANT: Prevent VIVO freeze by releasing camera COMPLETELY
    override fun onDestroy() {
        super.onDestroy()

        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {
        }
        try {
            preview?.setSurfaceProvider(null)
        } catch (_: Exception) {
        }
        try {
            imgCaptureExecutor.shutdown()
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val REQUEST_ID = "REQUEST_ID"

        fun getIntent(context: Context, requestId: Int): Intent {
            return Intent(context, CameraActivity::class.java).apply {
                putExtra(REQUEST_ID, requestId)
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
