package com.example.presentation.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AntiCheatingEngine
import com.example.core.DeviceSecurityChecker
import com.example.core.LockdownManager
import com.example.core.ServerTimerManager
import com.example.data.model.*
import com.example.data.repository.ExamRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Splash : ScreenDestination()
    object WelcomeSecurityCheck : ScreenDestination()
    object Login : ScreenDestination()
    object StudentDashboard : ScreenDestination()
    object ExamList : ScreenDestination()
    data class ExamDetail(val exam: Exam) : ScreenDestination()
    data class ExamInstruction(val exam: Exam) : ScreenDestination()
    data class ExamTaking(val exam: Exam) : ScreenDestination()
    data class ExamResult(val exam: Exam, val score: Double, val total: Double, val grade: String) : ScreenDestination()
    object ExamHistory : ScreenDestination()
    object StudentProfile : ScreenDestination()
    object LecturerDashboard : ScreenDestination() // Dosen & Admin Console
}

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamRepository(application)

    // Current Navigation State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Auth & User Profile State
    private val _currentUser = MutableStateFlow(
        User(
            id = "usr_std_01",
            username = "202400101",
            fullName = "Budi Pratama",
            role = UserRole.STUDENT,
            identifier = "202400101",
            studyProgram = "S1 Teknik Informatika",
            className = "TI-3A"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Device Integrity Report
    private val _deviceReport = MutableStateFlow(DeviceSecurityChecker.checkDevice(application))
    val deviceReport: StateFlow<DeviceSecurityReport> = _deviceReport.asStateFlow()

    // Exams
    val exams: List<Exam> = repository.getAssignedExams()
    val isOnline: StateFlow<Boolean> = repository.isOnline

    // Active Exam Session State
    private val _selectedExam = MutableStateFlow<Exam?>(null)
    val selectedExam: StateFlow<Exam?> = _selectedExam.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _answers = MutableStateFlow<Map<String, StudentAnswer>>(emptyMap())
    val answers: StateFlow<Map<String, StudentAnswer>> = _answers.asStateFlow()

    private val _isSubmitConfirmationVisible = MutableStateFlow(false)
    val isSubmitConfirmationVisible: StateFlow<Boolean> = _isSubmitConfirmationVisible.asStateFlow()

    private val _isQuestionPaletteOpen = MutableStateFlow(false)
    val isQuestionPaletteOpen: StateFlow<Boolean> = _isQuestionPaletteOpen.asStateFlow()

    // Anti-Cheating & Lockdown
    var lockdownManager: LockdownManager? = null
    val antiCheatingEngine = AntiCheatingEngine(
        studentName = _currentUser.value.fullName,
        onViolationLogged = { violation ->
            viewModelScope.launch {
                repository.saveViolationLocally(violation)
            }
        }
    )

    // Authoritative Server Timer
    lateinit var timerManager: ServerTimerManager

    // Lecturer & Admin Features
    val liveStudents: StateFlow<List<LiveStudentExamProgress>> = repository.liveExamStudents
    val questionBank: StateFlow<List<Question>> = repository.questionBank

    private val _googleFormUrl = MutableStateFlow("")
    val googleFormUrl: StateFlow<String> = _googleFormUrl.asStateFlow()

    private val _googleFormPreviewQuestions = MutableStateFlow<List<Question>>(emptyList())
    val googleFormPreviewQuestions: StateFlow<List<Question>> = _googleFormPreviewQuestions.asStateFlow()

    private val _lecturerActiveTab = MutableStateFlow(0) // 0: Live Monitoring, 1: Anti-Cheating Center, 2: Question Bank, 3: Google Form Import
    val lecturerActiveTab: StateFlow<Int> = _lecturerActiveTab.asStateFlow()

    init {
        timerManager = ServerTimerManager(
            scope = viewModelScope,
            onTimeExpired = {
                handleAutoSubmit("Waktu ujian telah habis! Jawaban Anda otomatis dikumpulkan.")
            },
            onClockTamperDetected = { offset ->
                antiCheatingEngine.recordTimeManipulation(offset)
            }
        )
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun refreshDeviceCheck() {
        _deviceReport.value = DeviceSecurityChecker.checkDevice(getApplication())
    }

    fun switchUserRole(role: UserRole) {
        when (role) {
            UserRole.STUDENT -> {
                _currentUser.value = User(
                    id = "usr_std_01",
                    username = "202400101",
                    fullName = "Budi Pratama",
                    role = UserRole.STUDENT,
                    identifier = "202400101",
                    studyProgram = "S1 Teknik Informatika",
                    className = "TI-3A"
                )
                _currentScreen.value = ScreenDestination.StudentDashboard
            }
            UserRole.LECTURER -> {
                _currentUser.value = User(
                    id = "usr_lec_01",
                    username = "19800512",
                    fullName = "Dr. Ir. Hendra Gunawan, M.T.",
                    role = UserRole.LECTURER,
                    identifier = "198005122005011002",
                    studyProgram = "Teknik Informatika",
                    className = "Dosen Pengampu"
                )
                _currentScreen.value = ScreenDestination.LecturerDashboard
            }
            UserRole.ADMIN -> {
                _currentUser.value = User(
                    id = "usr_adm_01",
                    username = "admin_puskom",
                    fullName = "Super Administrator Puskom",
                    role = UserRole.ADMIN,
                    identifier = "ADM-001",
                    studyProgram = "Biro Akademik & Puskom",
                    className = "Administrator"
                )
                _currentScreen.value = ScreenDestination.LecturerDashboard
            }
        }
    }

    fun toggleNetworkSimulation() {
        val next = !repository.isOnline.value
        repository.setNetworkStatus(next)
        antiCheatingEngine.recordNetworkStatus(next)
        if (next) {
            viewModelScope.launch {
                repository.syncPendingQueue()
            }
        }
    }

    // Exam Flow Operations
    fun selectExam(exam: Exam) {
        _selectedExam.value = exam
        _questions.value = repository.getExamQuestions(exam.id)
        _currentQuestionIndex.value = 0
        _answers.value = emptyMap()
    }

    fun startExam(activity: Activity, exam: Exam) {
        selectExam(exam)
        lockdownManager = LockdownManager(activity)
        lockdownManager?.enableLockdown()
        antiCheatingEngine.startMonitoring()
        timerManager.startTimer(exam.durationMinutes)
        _currentScreen.value = ScreenDestination.ExamTaking(exam)
    }

    fun answerMultipleChoice(questionId: String, optionId: String) {
        val current = _answers.value[questionId]
        val updated = (current ?: StudentAnswer(questionId)).copy(
            selectedOptionId = optionId,
            savedAt = System.currentTimeMillis()
        )
        _answers.value = _answers.value + (questionId to updated)

        viewModelScope.launch {
            repository.saveAnswerLocally(
                attemptId = _selectedExam.value?.id ?: "attempt",
                questionId = questionId,
                selectedOptionId = optionId,
                essayText = updated.essayText,
                isFlagged = updated.isFlagged
            )
        }
    }

    fun answerEssay(questionId: String, text: String) {
        val current = _answers.value[questionId]
        val updated = (current ?: StudentAnswer(questionId)).copy(
            essayText = text,
            savedAt = System.currentTimeMillis()
        )
        _answers.value = _answers.value + (questionId to updated)

        viewModelScope.launch {
            repository.saveAnswerLocally(
                attemptId = _selectedExam.value?.id ?: "attempt",
                questionId = questionId,
                selectedOptionId = updated.selectedOptionId,
                essayText = text,
                isFlagged = updated.isFlagged
            )
        }
    }

    fun toggleFlagQuestion(questionId: String) {
        val current = _answers.value[questionId] ?: StudentAnswer(questionId)
        val updated = current.copy(isFlagged = !current.isFlagged)
        _answers.value = _answers.value + (questionId to updated)

        viewModelScope.launch {
            repository.saveAnswerLocally(
                attemptId = _selectedExam.value?.id ?: "attempt",
                questionId = questionId,
                selectedOptionId = updated.selectedOptionId,
                essayText = updated.essayText,
                isFlagged = updated.isFlagged
            )
        }
    }

    fun goToQuestion(index: Int) {
        if (index in 0 until _questions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun showSubmitConfirmation(show: Boolean) {
        _isSubmitConfirmationVisible.value = show
    }

    fun toggleQuestionPalette(open: Boolean) {
        _isQuestionPaletteOpen.value = open
    }

    fun submitExamFinal() {
        val exam = _selectedExam.value ?: return
        finishExamSession(exam)
    }

    private fun handleAutoSubmit(reason: String) {
        val exam = _selectedExam.value ?: return
        finishExamSession(exam)
    }

    private fun finishExamSession(exam: Exam) {
        lockdownManager?.disableLockdown()
        antiCheatingEngine.stopMonitoring()
        timerManager.stopTimer()
        _isSubmitConfirmationVisible.value = false
        _isQuestionPaletteOpen.value = false

        // Compute objective score
        val questionsList = _questions.value
        var earnedObjectiveScore = 0.0
        var totalPossibleScore = 0.0

        for (q in questionsList) {
            totalPossibleScore += q.maxScore
            if (q.type == QuestionType.MULTIPLE_CHOICE) {
                val studentAns = _answers.value[q.id]?.selectedOptionId
                val correctOption = q.options.find { it.isCorrect }?.id
                if (studentAns != null && studentAns == correctOption) {
                    earnedObjectiveScore += q.maxScore
                }
            } else {
                // Essay is marked for review
                earnedObjectiveScore += (q.maxScore * 0.8) // Simulated partial credit
            }
        }

        val percentage = (earnedObjectiveScore / totalPossibleScore * 100).coerceIn(0.0, 100.0)
        val grade = when {
            percentage >= 85.0 -> "A"
            percentage >= 75.0 -> "B"
            percentage >= 65.0 -> "C"
            percentage >= 50.0 -> "D"
            else -> "E"
        }

        _currentScreen.value = ScreenDestination.ExamResult(
            exam = exam,
            score = earnedObjectiveScore,
            total = totalPossibleScore,
            grade = grade
        )
    }

    // Google Form Parser Actions
    fun setGoogleFormUrl(url: String) {
        _googleFormUrl.value = url
    }

    fun previewGoogleForm() {
        val questions = repository.parseGoogleFormUrl(_googleFormUrl.value)
        _googleFormPreviewQuestions.value = questions
    }

    fun importGoogleFormQuestions() {
        repository.addQuestionsToBank(_googleFormPreviewQuestions.value)
        _googleFormPreviewQuestions.value = emptyList()
        _googleFormUrl.value = ""
        _lecturerActiveTab.value = 2 // Switch to Question Bank tab
    }

    fun setLecturerActiveTab(tab: Int) {
        _lecturerActiveTab.value = tab
    }
}
