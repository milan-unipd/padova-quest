package it.unipd.milan.padovaquest.feature_authentication.presentation.user_login

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionsHelper {
    private lateinit var foregroundPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var backgroundPermissionLauncher: ActivityResultLauncher<String>

    fun initialize(fragment: Fragment, onPermissionsGranted: () -> Unit) {
        foregroundPermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]
                ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission()
                } else {
                    onPermissionsGranted()
                }
            } else {
                Toast.makeText(fragment.requireContext(), "Foreground location permission is required.", Toast.LENGTH_SHORT).show()
            }
        }

        backgroundPermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onPermissionsGranted()
            } else {
                Toast.makeText(fragment.requireContext(), "Background location permission is required for full functionality.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkAndRequestPermissions(fragment: Fragment) {
        val context = fragment.requireContext()
        if (hasForegroundPermissions(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundPermission(context)) {
                requestBackgroundLocationPermission()
            } else {
                (fragment as? PermissionCallback)?.onPermissionsGranted()
            }
        } else {
            requestForegroundLocationPermissions()
        }
    }

    private fun requestForegroundLocationPermissions() {
        foregroundPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun hasForegroundPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasBackgroundPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    interface PermissionCallback {
        fun onPermissionsGranted()
    }

}