package com.example.core

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class LockdownManager(private val activity: Activity) {

    private val tag = "LockdownManager"
    private var isLockdownActive = false

    /**
     * Enables maximum possible lockdown mode for BYOD device:
     * 1. WindowManager.LayoutParams.FLAG_SECURE (Blocks screenshot and screen recording)
     * 2. WindowInsetsControllerCompat immersive sticky fullscreen (Hides status & navigation bars)
     * 3. Screen always on
     * 4. Android Lock Task Mode (if permitted by device/user)
     */
    fun enableLockdown(): Boolean {
        try {
            isLockdownActive = true

            // 1. FLAG_SECURE prevents screenshots & video captures
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // 2. Fullscreen Immersive Sticky
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            // 3. Lock Task Mode (Kiosk pinning) fallback
            try {
                val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val isLockTaskPermitted = activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
                if (!isLockTaskPermitted) {
                    activity.startLockTask()
                    Log.i(tag, "startLockTask invoked successfully")
                }
            } catch (e: Exception) {
                // On personal BYOD devices without Device Owner, startLockTask may prompt the user or throw security exception
                Log.w(tag, "Lock Task mode fallback active: ${e.message}")
            }

            return true
        } catch (e: Exception) {
            Log.e(tag, "Failed to enable lockdown", e)
            return false
        }
    }

    /**
     * Restores normal window flags and exits lock task mode when exam is finished
     */
    fun disableLockdown() {
        try {
            isLockdownActive = false

            // Remove FLAG_SECURE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Restore System Bars
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

            // Stop Lock Task
            try {
                activity.stopLockTask()
            } catch (e: Exception) {
                Log.w(tag, "stopLockTask exception: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to disable lockdown", e)
        }
    }

    fun isLockdownEnabled(): Boolean = isLockdownActive
}
