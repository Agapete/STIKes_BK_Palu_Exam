package com.example.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.example.data.model.DeviceSecurityReport
import com.example.data.model.SecurityLevel
import java.io.File

object DeviceSecurityChecker {

    fun checkDevice(context: Context): DeviceSecurityReport {
        val notes = mutableListOf<String>()

        // 1. Android Version
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notes.add("Versi Android di bawah 8.0 (fitur keamanan mungkin terbatas)")
        } else {
            notes.add("Versi Android memenuhi standar minimum (>= Android 8.0)")
        }

        // 2. Device Model
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

        // 3. Root Detection Heuristics
        val isRooted = checkRootHeuristics()
        if (isRooted) {
            notes.add("Peringatan: Tanda-tanda Root / Custom SuperSU terdeteksi")
        } else {
            notes.add("Perangkat tidak ter-root (Normal / Clean)")
        }

        // 4. Debug Mode (Developer Options ADB)
        val isDebugMode = checkAdbEnabled(context)
        if (isDebugMode) {
            notes.add("Peringatan: USB Debugging / Developer Options aktif")
        } else {
            notes.add("USB Debugging nonaktif")
        }

        // 5. Lock Task Capability
        val isLockTaskSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        notes.add("Fitur Layar Terkunci (Screen Pinning / Lock Task) didukung perangkat")

        // 6. Network Check
        val isNetworkSecure = checkNetworkConnectivity(context)
        if (!isNetworkSecure) {
            notes.add("Koneksi internet terputus saat ini")
        } else {
            notes.add("Koneksi jaringan internet stabil")
        }

        // Determine Level: GREEN, YELLOW, RED
        val level = when {
            isRooted -> SecurityLevel.RED
            isDebugMode || !isNetworkSecure -> SecurityLevel.YELLOW
            else -> SecurityLevel.GREEN
        }

        return DeviceSecurityReport(
            androidVersion = androidVersion,
            deviceModel = deviceModel,
            isRooted = isRooted,
            isDebugMode = isDebugMode,
            isLockTaskSupported = isLockTaskSupported,
            isNetworkSecure = isNetworkSecure,
            securityLevel = level,
            notes = notes
        )
    }

    private fun checkRootHeuristics(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        return false
    }

    private fun checkAdbEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED, 0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun checkNetworkConnectivity(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
