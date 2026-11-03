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
            studyProgram = "D3 Keperawatan",
            className = "KLP-A"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Device Integrity Report
    private val _deviceReport = MutableStateFlow(DeviceSecurityChecker.checkDevice(application))
    val deviceReport: StateFlow<DeviceSecurityReport> = _deviceReport.asStateFlow()

    // Exams
    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()
    val isOnline: StateFlow<Boolean> = repository.isOnline

    fun loadExams() {
        viewModelScope.launch {
            try {
                val response = com.example.data.remote.ApiClient.apiService.getExams()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val remoteExams = response.body()?.data ?: emptyList()
                    _exams.value = remoteExams.map { dto ->
                        Exam(
                            id = dto.id,
                            title = dto.title,
                            courseCode = dto.course,
                            courseName = dto.course,
                            lecturerName = dto.lecturer,
                            durationMinutes = dto.durationMinutes,
                            startTime = parseDateToMillis(dto.startTime),
                            endTime = parseDateToMillis(dto.endTime),
                            questionCount = dto.questionCount ?: 0,
                            passingGrade = 60.0,
                            instructions = "Ujian dimonitor ketat oleh Anti-Cheating Engine.",
                            status = when (dto.status) {
                                "SCHEDULED" -> ExamStatus.SCHEDULED
                                "ENDED" -> ExamStatus.ENDED
                                else -> ExamStatus.ACTIVE
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline local mockup or cache if needed
                e.printStackTrace()
            }
        }
    }

    private fun parseDateToMillis(isoString: String): Long {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch(e: Exception) {
            System.currentTimeMillis()
        }
    }

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
            currentAttemptId?.let { attemptId ->
                viewModelScope.launch {
                    try {
                        com.example.data.remote.ApiClient.apiService.reportViolation(
                            attemptId,
                            com.example.data.remote.ViolationRequest(
                                eventType = violation.eventType,
                                severity = violation.severity.name,
                                scoreIncrement = violation.scoreIncrement,
                                metadata = violation.description
                            )
                        )
                    } catch(e: Exception) { e.printStackTrace() }
                }
            }
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

    fun logout() {
        com.example.data.remote.ApiClient.authToken = null
        _currentUser.value = User(
            id = "",
            username = "",
            fullName = "",
            role = UserRole.STUDENT,
            identifier = ""
        )
        _currentScreen.value = ScreenDestination.Login
    }

    fun loginWithApi(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = com.example.data.remote.ApiClient.apiService.login(
                    com.example.data.remote.LoginRequest(username, password)
                )
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.status == "success" && data.user != null) {
                        // Store signed JWT token securely in ApiClient
                        com.example.data.remote.ApiClient.authToken = data.token
                        val newRole = when(data.user.role) {
                            "STUDENT" -> UserRole.STUDENT
                            "LECTURER" -> UserRole.LECTURER
                            else -> UserRole.ADMIN
                        }
                        
                        _currentUser.value = User(
                            id = data.user.id,
                            username = data.user.username,
                            fullName = data.user.name,
                            role = newRole,
                            identifier = data.user.username,
                            studyProgram = data.user.studyProgram ?: "D3 Keperawatan",
                            className = data.user.className ?: "KLP-A"
                        )
                        
                        _currentScreen.value = if (newRole == UserRole.STUDENT) {
                            loadExams()
                            ScreenDestination.StudentDashboard
                        } else {
                            ScreenDestination.LecturerDashboard
                        }
                        onResult(true, "Login berhasil")
                    } else {
                        onResult(false, data?.message ?: "Login gagal")
                    }
                } else {
                    onResult(false, "Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, "Gagal terhubung ke server: ${e.message}")
            }
        }
    }

    fun registerWithApi(fullName: String, username: String, studyProgram: String, className: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = com.example.data.remote.ApiClient.apiService.register(
                    com.example.data.remote.RegisterRequest(fullName, username, studyProgram, className)
                )
                if (response.isSuccessful && response.body()?.status == "success") {
                    onResult(true, response.body()?.message ?: "Pendaftaran berhasil!")
                } else {
                    onResult(false, response.body()?.message ?: "Gagal mendaftar.")
                }
            } catch (e: Exception) {
                onResult(false, "Gagal terhubung ke server: ${e.message}")
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

    private var currentAttemptId: String? = null

    // Exam Flow Operations
    fun selectExam(exam: Exam) {
        _selectedExam.value = exam
        _currentQuestionIndex.value = 0
        _answers.value = emptyMap()
    }

    fun startExam(activity: Activity, exam: Exam) {
        selectExam(exam)
        
        viewModelScope.launch {
            try {
                val response = com.example.data.remote.ApiClient.apiService.startExam(exam.id)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()
                    currentAttemptId = data?.attemptId
                    
                    val remoteQuestions = data?.questions?.mapIndexed { index, qDto ->
                        Question(
                            id = qDto.id,
                            number = index + 1,
                            text = qDto.text,
                            type = QuestionType.MULTIPLE_CHOICE,
                            options = qDto.options.map { oDto ->
                                QuestionOption(id = oDto.id, label = oDto.label, text = oDto.text)
                            }
                        )
                    } ?: emptyList()
                    
                    _questions.value = remoteQuestions
                } else {
                    _questions.value = repository.getExamQuestions(exam.id) // Fallback to mock
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _questions.value = repository.getExamQuestions(exam.id) // Fallback to mock
            }
            
            lockdownManager = LockdownManager(activity)
            lockdownManager?.enableLockdown()
            antiCheatingEngine.startMonitoring()
            timerManager.startTimer(exam.durationMinutes)
            _currentScreen.value = ScreenDestination.ExamTaking(exam)
        }
    }

    fun answerMultipleChoice(questionId: String, optionId: String) {
        val current = _answers.value[questionId]
        val updated = (current ?: StudentAnswer(questionId)).copy(
            selectedOptionId = optionId,
            savedAt = System.currentTimeMillis()
        )
        _answers.value = _answers.value + (questionId to updated)

        // API Call
        currentAttemptId?.let { attemptId ->
            viewModelScope.launch {
                try {
                    com.example.data.remote.ApiClient.apiService.submitAnswer(
                        attemptId, 
                        com.example.data.remote.AnswerRequest(questionId, optionId)
                    )
                } catch(e: Exception) { e.printStackTrace() }
            }
        }

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

        val attemptId = currentAttemptId
        if (attemptId != null) {
            viewModelScope.launch {
                try {
                    val response = com.example.data.remote.ApiClient.apiService.submitExam(attemptId)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val result = response.body()?.data
                        _currentScreen.value = ScreenDestination.ExamResult(
                            exam = exam,
                            score = result?.finalScore ?: 0.0,
                            total = 100.0,
                            grade = result?.grade ?: "C"
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Fallback result display
                _currentScreen.value = ScreenDestination.ExamResult(
                    exam = exam,
                    score = 0.0,
                    total = 100.0,
                    grade = "-"
                )
            }
        } else {
            _currentScreen.value = ScreenDestination.ExamResult(
                exam = exam,
                score = 0.0,
                total = 100.0,
                grade = "-"
            )
        }
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
