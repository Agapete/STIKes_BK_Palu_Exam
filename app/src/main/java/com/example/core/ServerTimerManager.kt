package com.example.core

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class ServerTimerManager(
    private val scope: CoroutineScope,
    private val onTimeExpired: () -> Unit,
    private val onClockTamperDetected: (Long) -> Unit
) {
    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _formattedTime = MutableStateFlow("00:00:00")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private var initialElapsedRealtime: Long = 0
    private var initialServerTime: Long = 0
    private var serverEndTime: Long = 0

    /**
     * Synchronizes with server-side exam duration.
     * Uses SystemClock.elapsedRealtime() to prevent local clock manipulation.
     */
    fun startTimer(durationMinutes: Int, serverNowTimestamp: Long = System.currentTimeMillis()) {
        timerJob?.cancel()

        val totalDurationSeconds = durationMinutes * 60L
        serverEndTime = serverNowTimestamp + (totalDurationSeconds * 1000L)
        initialServerTime = serverNowTimestamp
        initialElapsedRealtime = SystemClock.elapsedRealtime()

        _remainingSeconds.value = totalDurationSeconds
        updateFormattedTime(totalDurationSeconds)

        timerJob = scope.launch {
            while (isActive) {
                delay(1000)

                // Monotonic elapsed time calculation
                val elapsedSinceStart = (SystemClock.elapsedRealtime() - initialElapsedRealtime) / 1000
                val remaining = (totalDurationSeconds - elapsedSinceStart).coerceAtLeast(0)

                // Detect if system clock moved backwards/forwards abnormally
                val currentWallTime = System.currentTimeMillis()
                val expectedWallTime = initialServerTime + (elapsedSinceStart * 1000)
                val diffSeconds = Math.abs(currentWallTime - expectedWallTime) / 1000
                if (diffSeconds > 15) {
                    onClockTamperDetected(diffSeconds)
                }

                _remainingSeconds.value = remaining
                updateFormattedTime(remaining)

                if (remaining <= 0) {
                    onTimeExpired()
                    break
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateFormattedTime(seconds: Long) {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        _formattedTime.value = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    }
}
