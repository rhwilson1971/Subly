package net.cynreub.subly.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android 12 and below
            true
        }
    }

    fun shouldShowPermissionRationale(): Boolean {
        // This will be checked in MainActivity/SettingsScreen
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
