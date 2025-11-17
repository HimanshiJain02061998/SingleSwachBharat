package com.appynitty.kotlinsbalibrary.common.utils.permission

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class LocationPermission(private val activity: AppCompatActivity) {

    fun initFineLocationPermission(context: Context) {

        when {
            isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                requestBackgroundIfNeeded()
            }

            shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showFineLocationRationale()
            }

            else -> {
                // User pressed “Don't ask again”
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Check if granted
    private fun isGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Check rationale
    private fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    // Ask for background location after fine location granted
    private fun requestBackgroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            when {
                isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                    // LocationUtils.startLocationTracking(activity)
                }

                shouldShowRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                    showBackgroundLocationRationale()
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
        }
    }

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

            if (granted) {
                requestBackgroundIfNeeded()

            } else {
                // Permission denied
                if (!shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // PERMANENTLY DENIED → Open Settings
                    showSettingsRedirectDialog()
                } else {
                    showFineLocationRationale()
                }
            }
        }

    // Force user strongly to grant Fine Location
    private fun showFineLocationRationale() {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Required")
            .setMessage(
                "This app requires location permission to function properly. " +
                        "Please tap ALLOW to continue."
            )
            .setCancelable(false)
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Exit App") { _, _ ->
                activity.finish()
            }
            .show()
    }

    // Forceful dialog for background location
    private fun showBackgroundLocationRationale() {
        AlertDialog.Builder(activity)
            .setTitle("Background Location Required")
            .setMessage(
                "Please select **Allow all the time** for accurate tracking in background."
            )
            .setCancelable(false)
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton("Exit App") { _, _ ->
                activity.finish()
            }
            .show()
    }

    // When user permanently denies
    private fun showSettingsRedirectDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Needed")
            .setMessage(
                "You have permanently denied the permission.\n\n" +
                        "Go to Settings → Permissions → Allow Location."
            )
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Exit App") { _, _ ->
                activity.finish()
            }
            .show()
    }

    // Redirect to app settings
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }
}
