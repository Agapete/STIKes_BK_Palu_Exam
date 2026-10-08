package com.example.core

import com.example.data.model.ViolationCategory
import com.example.data.model.ViolationEvent
import com.example.data.model.ViolationSeverity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AntiCheatingEngine(
    private val studentName: String = "Mahasiswa",
    private val onViolationLogged: ((ViolationEvent) -> Unit)? = null
) {
    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore.asStateFlow()

    private val _category = MutableStateFlow(ViolationCategory.NORMAL)
    val category: StateFlow<ViolationCategory> = _category.asStateFlow()

    private val _recentViolations = MutableStateFlow<List<ViolationEvent>>(emptyList())
    val recentViolations: StateFlow<List<ViolationEvent>> = _recentViolations.asStateFlow()

    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    private var backgroundTimestamp: Long = 0
    private var isExamActive: Boolean = false

    fun startMonitoring(initialScore: Int = 0) {
        isExamActive = true
        _totalScore.value = initialScore
        updateCategory(initialScore)
    }

    fun stopMonitoring() {
        isExamActive = false
        _activeAlert.value = null
    }

    fun recordAppBackground() {
        if (!isExamActive) return
        backgroundTimestamp = System.currentTimeMillis()
    }

    fun recordAppForeground() {
        if (!isExamActive) return
        val now = System.currentTimeMillis()
        val durationMs = if (backgroundTimestamp > 0) now - backgroundTimestamp else 0
        backgroundTimestamp = 0

        val seconds = (durationMs / 1000).coerceAtLeast(1)
        logViolation(
            eventType = "APP_BACKGROUND",
            severity = if (seconds > 10) ViolationSeverity.HIGH else ViolationSeverity.MEDIUM,
            scoreIncrement = 20,
            durationMs = durationMs,
            description = "Aplikasi diminimalkan / pindah background selama ${seconds} detik."
        )
        _activeAlert.value = "Peringatan: Anda meninggalkan aplikasi selama ${seconds} detik! Kejadian ini dicatat pengawas."
    }

    fun recordWindowFocusLost() {
        if (!isExamActive) return
        logViolation(
            eventType = "WINDOW_FOCUS_LOST",
            severity = ViolationSeverity.MEDIUM,
            scoreIncrement = 30,
            durationMs = 0,
            description = "Fokus layar terputus (notifikasi bar ditarik atau split-screen aktif)."
        )
        _activeAlert.value = "Perhatian: Panel notifikasi / floating window terdeteksi. Harap fokus pada soal."
    }

    fun recordScreenshotAttempt() {
        if (!isExamActive) return
        logViolation(
            eventType = "SCREEN_CAPTURE_ATTEMPT",
            severity = ViolationSeverity.HIGH,
            scoreIncrement = 30,
            durationMs = 0,
            description = "Percobaan screenshot / tangkapan layar diblokir."
        )
        _activeAlert.value = "Peringatan Serius: Percobaan screenshot terdeteksi dan diblokir oleh sistem."
    }

    fun recordScreenRecordingDetected() {
        if (!isExamActive) return
        logViolation(
            eventType = "SCREEN_RECORDING_DETECTED",
            severity = ViolationSeverity.CRITICAL,
            scoreIncrement = 30,
            durationMs = 0,
            description = "Layanan perekaman layar terdeteksi aktif."
        )
        _activeAlert.value = "Peringatan Kritis: Perekam layar terdeteksi aktif!"
    }

    fun recordNetworkStatus(isConnected: Boolean) {
        if (!isExamActive) return
        if (!isConnected) {
            logViolation(
                eventType = "NETWORK_DISCONNECTED",
                severity = ViolationSeverity.LOW,
                scoreIncrement = 5,
                durationMs = 0,
                description = "Koneksi internet terputus. Mode offline diaktifkan."
            )
        } else {
            logViolation(
                eventType = "NETWORK_RECONNECTED",
                severity = ViolationSeverity.LOW,
                scoreIncrement = 0,
                durationMs = 0,
                description = "Koneksi internet kembali tersambung. Sinkronisasi berjalan."
            )
        }
    }

    fun recordTimeManipulation(offsetSeconds: Long) {
        if (!isExamActive) return
        logViolation(
            eventType = "TIME_MANIPULATION",
            severity = ViolationSeverity.HIGH,
            scoreIncrement = 20,
            durationMs = 0,
            description = "Perubahan jam lokal perangkat sebesar ${offsetSeconds} detik terdeteksi."
        )
        _activeAlert.value = "Peringatan: Perubahan jam perangkat tidak dapat menambah durasi ujian server."
    }

    fun dismissAlert() {
        _activeAlert.value = null
    }

    private fun logViolation(
        eventType: String,
        severity: ViolationSeverity,
        scoreIncrement: Int,
        durationMs: Long,
        description: String
    ) {
        val newScore = _totalScore.value + scoreIncrement
        _totalScore.value = newScore
        updateCategory(newScore)

        val event = ViolationEvent(
            id = UUID.randomUUID().toString(),
            attemptId = "current_attempt",
            studentName = studentName,
            eventType = eventType,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
            severity = severity,
            scoreIncrement = scoreIncrement,
            description = description
        )

        _recentViolations.value = listOf(event) + _recentViolations.value.take(15)
        onViolationLogged?.invoke(event)
    }

    private fun updateCategory(score: Int) {
        _category.value = when {
            score <= 20 -> ViolationCategory.NORMAL
            score <= 50 -> ViolationCategory.PERLU_DIPERIKSA
            score <= 80 -> ViolationCategory.MENCURIGAKAN
            else -> ViolationCategory.PELANGGARAN_BERAT
        }
    }
}
