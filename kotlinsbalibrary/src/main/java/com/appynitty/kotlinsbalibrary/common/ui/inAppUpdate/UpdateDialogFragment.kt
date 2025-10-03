package com.appynitty.kotlinsbalibrary.common.ui.inAppUpdate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.CustomToast
import com.appynitty.kotlinsbalibrary.databinding.FragmentUpdateDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class UpdateDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentUpdateDialogBinding

    private val viewModel: UpdateViewModel by viewModels()
    private lateinit var downloadManager: DownloadManager

    var downloadLink: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUpdateDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        const val TAG = "UpdateDialogFragment"
    }

    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        initPermission()
        binding.updateBtn.setOnClickListener {

            viewModel.startDownloadingUpdate(downloadLink, downloadManager)
        }

        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.updateProgress.collect { updateProgress ->

                    binding.linearProgressBar.progress = updateProgress.progress
                    binding.progressPercentTv.text = buildString {
                        append(updateProgress.progress)
                        append(" %")
                    }
                    if (updateProgress.progress == 100) {
                        binding.updateTxt.text =
                            resources.getString(R.string.download_completed)
                        installUpdatedAPK(updateProgress.downloadId)
                    }

                }
            }

            launch {
                viewModel.updateEventsFlow.collect { event ->
                    when (event) {
                        is UpdateViewModel.UpdateDialogEvent.ShowErrorMsg -> {
                            CustomToast.showErrorToast(requireContext(), event.msg)
                        }

                        UpdateViewModel.UpdateDialogEvent.ShowProgressBar -> {

                            binding.progressPercentTv.visibility = View.VISIBLE
                            binding.linearProgressBar.visibility = View.VISIBLE
                            binding.updateTxt.text =
                                resources.getString(R.string.download_in_progres)
                            binding.updateBtn.visibility = View.INVISIBLE
                        }
                    }
                }
            }

        }

    }

    @SuppressLint("Range")
    private fun installUpdatedAPK(downloadId: Long) {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val c = downloadManager.query(query)
        if (c.moveToFirst()) {
            val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                val uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                //TODO : Use this local uri and launch intent to open file
                val apkUri = Uri.parse(uriString)
                //viewModel.install(apkUri)
                val file = apkUri.path?.let { File(it) }
                if (file != null) {
                    installUpdate(file)
                }
            }
        }
    }

    private fun installUpdate(file: File) {
        val appId = requireActivity().packageName

        val apkUri =
            FileProvider.getUriForFile(
                requireContext().applicationContext,
                appId + ".provider",
                file
            )
        val intent: Intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.data = apkUri

        //   intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //todo try this : SANATH
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        appUpdateLauncher.launch(intent)
    }

    private var appUpdateLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    // User has allowed app to install APKs
//                    // so we can now launch APK installation.
//                    Toast.makeText(this, "App Updated Successfully !!", Toast.LENGTH_SHORT).show();
//
        if (result.resultCode == Activity.RESULT_CANCELED) {
            //     Toast.makeText(this, "User pressed 'Done' button", Toast.LENGTH_SHORT).show();
            Log.d(TAG, ": hi")
        } else if (result.resultCode == Activity.RESULT_OK) {
            // resultCode == RESULT_OK means user pressed `Open` button
            //  Toast.makeText(this, "User pressed 'Open' button", Toast.LENGTH_SHORT).show();
//                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appid);
//                    if (launchIntent != null) {
//                        startActivity(launchIntent);
//                    }

            Log.d(TAG, ": h3llo")

        }
    }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) {
                    //showPermissionRequestDialog(it.key)

                }
            }
        }


    private fun initPermission() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}