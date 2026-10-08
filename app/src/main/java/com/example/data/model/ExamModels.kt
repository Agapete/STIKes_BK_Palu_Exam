package com.example.data.model

import java.io.Serializable

enum class UserRole {
    STUDENT,
    LECTURER,
    ADMIN
}

enum class ExamStatus {
    DRAFT,
    SCHEDULED,
    ACTIVE,
    ENDED,
    ARCHIVED
}

enum class AttemptState {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    AUTO_SUBMITTED,
    EXPIRED,
    CANCELLED
}

enum class QuestionType {
    MULTIPLE_CHOICE,
    ESSAY
}

enum class ViolationSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ViolationCategory {
    NORMAL,            // 0 - 20
    PERLU_DIPERIKSA,   // 21 - 50
    MENCURIGAKAN,      // 51 - 80
    PELANGGARAN_BERAT  // 81+
}

enum class SecurityLevel {
    GREEN,  // Fully compliant
    YELLOW, // Minor limitations (e.g. non-MDM device, standard lock task)
    RED     // Insecure (e.g. Rooted / Dangerous debugger attached)
}

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val identifier: String, // NIM or NIP
    val studyProgram: String = "Teknik Informatika",
    val className: String = "TI-3A"
)

data class QuestionOption(
    val id: String,
    val label: String, // A, B, C, D, E
    val text: String,
    val isCorrect: Boolean = false
)

data class Question(
    val id: String,
    val number: Int,
    val type: QuestionType,
    val text: String,
    val options: List<QuestionOption> = emptyList(),
    val maxScore: Double = 1.0,
    val category: String = "Umum",
    val difficulty: String = "MEDIUM" // EASY, MEDIUM, HARD
)

data class Exam(
    val id: String,
    val title: String,
    val courseCode: String,
    val courseName: String,
    val lecturerName: String,
    val durationMinutes: Int,
    val startTime: Long,
    val endTime: Long,
    val questionCount: Int,
    val passingGrade: Double,
    val instructions: String,
    val allowBackNavigation: Boolean = true,
    val allowQuestionJump: Boolean = true,
    val showResult: Boolean = true,
    val antiCheatingEnabled: Boolean = true,
    val status: ExamStatus = ExamStatus.ACTIVE
)

data class StudentAnswer(
    val questionId: String,
    val selectedOptionId: String? = null,
    val essayText: String? = null,
    val isFlagged: Boolean = false,
    val savedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

data class ViolationEvent(
    val id: String,
    val attemptId: String,
    val studentName: String,
    val eventType: String,
    val timestamp: Long,
    val durationMs: Long,
    val severity: ViolationSeverity,
    val scoreIncrement: Int,
    val description: String
)

data class DeviceSecurityReport(
    val androidVersion: String,
    val deviceModel: String,
    val isRooted: Boolean,
    val isDebugMode: Boolean,
    val isLockTaskSupported: Boolean,
    val isNetworkSecure: Boolean,
    val securityLevel: SecurityLevel,
    val notes: List<String>
)

data class LiveStudentExamProgress(
    val studentId: String,
    val studentName: String,
    val nim: String,
    val answeredCount: Int,
    val totalQuestions: Int,
    val remainingSeconds: Long,
    val isOnline: Boolean,
    val securityLevel: SecurityLevel,
    val violationScore: Int,
    val category: ViolationCategory,
    val lastActiveTimestamp: Long
)
