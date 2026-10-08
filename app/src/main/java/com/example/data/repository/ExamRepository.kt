package com.example.data.repository

import android.content.Context
import com.example.data.local.ExamDatabase
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.ViolationEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ExamRepository(context: Context) {

    private val db = ExamDatabase.getInstance(context)
    private val answerDao = db.answerDao()
    private val violationDao = db.violationDao()
    private val syncQueueDao = db.syncQueueDao()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _liveExamStudents = MutableStateFlow<List<LiveStudentExamProgress>>(emptyList())
    val liveExamStudents: StateFlow<List<LiveStudentExamProgress>> = _liveExamStudents.asStateFlow()

    private val _questionBank = MutableStateFlow<List<Question>>(emptyList())
    val questionBank: StateFlow<List<Question>> = _questionBank.asStateFlow()

    init {
        initSampleQuestionBank()
        initLiveMonitoringData()
    }

    fun setNetworkStatus(online: Boolean) {
        _isOnline.value = online
    }

    /**
     * Retrieves mock & remote-ready exams assigned to student
     */
    fun getAssignedExams(): List<Exam> {
        val now = System.currentTimeMillis()
        return listOf(
            Exam(
                id = "exam_uts_algo",
                title = "UTS Algoritma & Struktur Data",
                courseCode = "IF-2104",
                courseName = "Algoritma & Struktur Data",
                lecturerName = "Dr. Ir. Hendra Gunawan, M.T.",
                durationMinutes = 60,
                startTime = now - (10 * 60 * 1000), // active right now
                endTime = now + (50 * 60 * 1000),
                questionCount = 10,
                passingGrade = 65.0,
                instructions = "1. Ujian dilaksanakan dalam Mode Terkunci (Lockdown Mode).\n" +
                        "2. Screenshot dan pembukaan aplikasi lain dilarang keras.\n" +
                        "3. Segala perpindahan aplikasi akan dicatat oleh AntiCheatingEngine.\n" +
                        "4. Jawaban tersimpan otomatis secara berkala.",
                allowBackNavigation = true,
                allowQuestionJump = true,
                showResult = true,
                antiCheatingEnabled = true,
                status = ExamStatus.ACTIVE
            ),
            Exam(
                id = "exam_uas_basisdata",
                title = "UAS Sistem Basis Data Terdistribusi",
                courseCode = "IF-3108",
                courseName = "Sistem Basis Data",
                lecturerName = "Prof. Siti Aminah, Ph.D.",
                durationMinutes = 90,
                startTime = now + (24 * 3600 * 1000), // tomorrow
                endTime = now + (26 * 3600 * 1000),
                questionCount = 25,
                passingGrade = 70.0,
                instructions = "Mencakup materi Normalisasi, Transaksi ACID, dan Sharding.",
                status = ExamStatus.SCHEDULED
            ),
            Exam(
                id = "exam_kuis_jarkom",
                title = "Kuis Jaringan Komputer & Keamanan Siber",
                courseCode = "IF-1202",
                courseName = "Jaringan Komputer",
                lecturerName = "Ahmad Fadhil, M.Kom.",
                durationMinutes = 45,
                startTime = now - (48 * 3600 * 1000), // finished
                endTime = now - (47 * 3600 * 1000),
                questionCount = 15,
                passingGrade = 60.0,
                instructions = "Kuis subnetting dan protokol TCP/IP.",
                status = ExamStatus.ENDED
            )
        )
    }

    /**
     * Provides exam questions including Multiple Choice and Essay
     */
    fun getExamQuestions(examId: String): List<Question> {
        return listOf(
            Question(
                id = "q1",
                number = 1,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Kompleksitas waktu rata-rata untuk algoritma QuickSort dalam notasi Big-O adalah:",
                options = listOf(
                    QuestionOption("q1_opt_a", "A", "O(n)", isCorrect = false),
                    QuestionOption("q1_opt_b", "B", "O(n log n)", isCorrect = true),
                    QuestionOption("q1_opt_c", "C", "O(n^2)", isCorrect = false),
                    QuestionOption("q1_opt_d", "D", "O(log n)", isCorrect = false),
                    QuestionOption("q1_opt_e", "E", "O(1)", isCorrect = false)
                ),
                maxScore = 10.0,
                category = "Algoritma"
            ),
            Question(
                id = "q2",
                number = 2,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Struktur data manakah yang mengimplementasikan prinsip First In First Out (FIFO)?",
                options = listOf(
                    QuestionOption("q2_opt_a", "A", "Stack", isCorrect = false),
                    QuestionOption("q2_opt_b", "B", "Queue", isCorrect = true),
                    QuestionOption("q2_opt_c", "C", "Tree", isCorrect = false),
                    QuestionOption("q2_opt_d", "D", "Graph", isCorrect = false),
                    QuestionOption("q2_opt_e", "E", "Priority Heap", isCorrect = false)
                ),
                maxScore = 10.0,
                category = "Struktur Data"
            ),
            Question(
                id = "q3",
                number = 3,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Manakah dari struktur data berikut yang paling efisien untuk mencari elemen dengan key unik dalam O(1) average case?",
                options = listOf(
                    QuestionOption("q3_opt_a", "A", "Binary Search Tree", isCorrect = false),
                    QuestionOption("q3_opt_b", "B", "Linked List", isCorrect = false),
                    QuestionOption("q3_opt_c", "C", "Hash Table", isCorrect = true),
                    QuestionOption("q3_opt_d", "D", "Array Linear", isCorrect = false),
                    QuestionOption("q3_opt_e", "E", "Trie", isCorrect = false)
                ),
                maxScore = 10.0,
                category = "Struktur Data"
            ),
            Question(
                id = "q4",
                number = 4,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Apa kondisi terburuk (worst-case) yang menyebabkan Binary Search Tree menjadi seperti Linked List biasa dengan kompleksitas O(n)?",
                options = listOf(
                    QuestionOption("q4_opt_a", "A", "Elemen diinput secara acak merata", isCorrect = false),
                    QuestionOption("q4_opt_b", "B", "Elemen diinput dalam urutan terurut menaik atau menurun", isCorrect = true),
                    QuestionOption("q4_opt_c", "C", "Pohon memiliki tinggi O(log n)", isCorrect = false),
                    QuestionOption("q4_opt_d", "D", "Jumlah node genap", isCorrect = false),
                    QuestionOption("q4_opt_e", "E", "Node akar memiliki dua anak", isCorrect = false)
                ),
                maxScore = 10.0,
                category = "Tree & Graph"
            ),
            Question(
                id = "q5",
                number = 5,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Metode penjelajahan graf yang menggunakan struktur data Queue adalah:",
                options = listOf(
                    QuestionOption("q5_opt_a", "A", "Depth First Search (DFS)", isCorrect = false),
                    QuestionOption("q5_opt_b", "B", "Breadth First Search (BFS)", isCorrect = true),
                    QuestionOption("q5_opt_c", "C", "Dijkstra Algorithm tanpa Heap", isCorrect = false),
                    QuestionOption("q5_opt_d", "D", "Bellman-Ford", isCorrect = false),
                    QuestionOption("q5_opt_e", "E", "Kruskal Algorithm", isCorrect = false)
                ),
                maxScore = 10.0,
                category = "Graph"
            ),
            Question(
                id = "q6",
                number = 6,
                type = QuestionType.ESSAY,
                text = "Jelaskan perbedaan mendasar antara Array Statis dan Singly Linked List dalam hal alokasi memori serta performa operasi penyisipan (insertion) di awal data!",
                options = emptyList(),
                maxScore = 25.0,
                category = "Analisis Kompleksitas"
            ),
            Question(
                id = "q7",
                number = 7,
                type = QuestionType.ESSAY,
                text = "Uraikan prinsip Divide and Conquer pada algoritma MergeSort dan jelaskan mengapa algoritma ini memiliki kompleksitas waktu yang stabil O(n log n) di semua kasus!",
                options = emptyList(),
                maxScore = 25.0,
                category = "Algoritma"
            )
        )
    }

    // Room Database local answer handling
    fun getSavedAnswersFlow(attemptId: String): Flow<Map<String, StudentAnswer>> {
        return answerDao.getAnswersForAttempt(attemptId).map { entities ->
            entities.associate { entity ->
                entity.questionId to StudentAnswer(
                    questionId = entity.questionId,
                    selectedOptionId = entity.selectedOptionId,
                    essayText = entity.essayText,
                    isFlagged = entity.isFlagged,
                    savedAt = entity.savedAt,
                    isSynced = entity.isSynced
                )
            }
        }
    }

    suspend fun saveAnswerLocally(
        attemptId: String,
        questionId: String,
        selectedOptionId: String?,
        essayText: String?,
        isFlagged: Boolean
    ) {
        val entity = AnswerEntity(
            questionId = questionId,
            attemptId = attemptId,
            selectedOptionId = selectedOptionId,
            essayText = essayText,
            isFlagged = isFlagged,
            savedAt = System.currentTimeMillis(),
            isSynced = _isOnline.value
        )
        answerDao.insertAnswer(entity)

        // If offline, enqueue to sync queue
        if (!_isOnline.value) {
            val syncPayload = "{\"questionId\":\"$questionId\",\"selectedOptionId\":\"$selectedOptionId\"}"
            syncQueueDao.enqueueItem(
                SyncQueueEntity(
                    attemptId = attemptId,
                    payloadType = "ANSWER",
                    payloadJson = syncPayload
                )
            )
        }
    }

    suspend fun saveViolationLocally(violation: ViolationEvent) {
        violationDao.insertViolation(
            ViolationEntity(
                attemptId = violation.attemptId,
                eventType = violation.eventType,
                timestamp = violation.timestamp,
                durationMs = violation.durationMs,
                scoreIncrement = violation.scoreIncrement,
                severity = violation.severity.name,
                description = violation.description
            )
        )
    }

    suspend fun syncPendingQueue() {
        val pending = syncQueueDao.getPendingSyncItems()
        if (pending.isNotEmpty() && _isOnline.value) {
            // Process queue
            syncQueueDao.clearQueue()
        }
    }

    // Google Form Parser Simulation
    fun parseGoogleFormUrl(formUrl: String): List<Question> {
        val formId = if (formUrl.contains("/forms/d/e/")) {
            formUrl.substringAfter("/forms/d/e/").substringBefore("/")
        } else if (formUrl.isNotBlank()) {
            formUrl.takeLast(12)
        } else {
            "GF-FORM-DEMO"
        }

        return listOf(
            Question(
                id = "gf_q1_${System.currentTimeMillis()}",
                number = 1,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "[Imported dari Google Form $formId] Protokol layer transport apa yang menyediakan layanan connection-oriented dan reliable?",
                options = listOf(
                    QuestionOption("gf_opt1_a", "A", "UDP (User Datagram Protocol)", false),
                    QuestionOption("gf_opt1_b", "B", "TCP (Transmission Control Protocol)", true),
                    QuestionOption("gf_opt1_c", "C", "IP (Internet Protocol)", false),
                    QuestionOption("gf_opt1_d", "D", "ICMP", false)
                ),
                maxScore = 1.0,
                category = "Import Google Form",
                difficulty = "EASY"
            ),
            Question(
                id = "gf_q2_${System.currentTimeMillis()}",
                number = 2,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "[Imported dari Google Form] Panjang bit alamat IPv6 adalah:",
                options = listOf(
                    QuestionOption("gf_opt2_a", "A", "32 bit", false),
                    QuestionOption("gf_opt2_b", "B", "64 bit", false),
                    QuestionOption("gf_opt2_c", "C", "128 bit", true),
                    QuestionOption("gf_opt2_d", "D", "256 bit", false)
                ),
                maxScore = 1.0,
                category = "Import Google Form",
                difficulty = "EASY"
            ),
            Question(
                id = "gf_q3_${System.currentTimeMillis()}",
                number = 3,
                type = QuestionType.ESSAY,
                text = "[Imported Paragraf] Jelaskan mekanisme 3-Way Handshake pada protokol TCP!",
                options = emptyList(),
                maxScore = 5.0,
                category = "Import Google Form",
                difficulty = "MEDIUM"
            )
        )
    }

    fun addQuestionsToBank(questions: List<Question>) {
        _questionBank.value = _questionBank.value + questions
    }

    private fun initSampleQuestionBank() {
        _questionBank.value = listOf(
            Question(
                id = "qb_1",
                number = 1,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Apa kepanjangan dari SQL?",
                options = listOf(
                    QuestionOption("qb_opt_1a", "A", "Structured Query Language", true),
                    QuestionOption("qb_opt_1b", "B", "Strong Question Language", false),
                    QuestionOption("qb_opt_1c", "C", "Simple Query Logic", false),
                    QuestionOption("qb_opt_1d", "D", "Standard Quick Link", false)
                ),
                category = "Basis Data"
            ),
            Question(
                id = "qb_2",
                number = 2,
                type = QuestionType.MULTIPLE_CHOICE,
                text = "Prinsip OOP yang menyembunyikan detail implementasi internal adalah:",
                options = listOf(
                    QuestionOption("qb_opt_2a", "A", "Encapsulation", true),
                    QuestionOption("qb_opt_2b", "B", "Inheritance", false),
                    QuestionOption("qb_opt_2c", "C", "Polymorphism", false),
                    QuestionOption("qb_opt_2d", "D", "Abstraction", false)
                ),
                category = "Pemrograman Berorientasi Objek"
            )
        )
    }

    private fun initLiveMonitoringData() {
        _liveExamStudents.value = listOf(
            LiveStudentExamProgress(
                studentId = "std_1",
                studentName = "Budi Pratama (Anda)",
                nim = "202400101",
                answeredCount = 5,
                totalQuestions = 10,
                remainingSeconds = 2950,
                isOnline = true,
                securityLevel = SecurityLevel.GREEN,
                violationScore = 0,
                category = ViolationCategory.NORMAL,
                lastActiveTimestamp = System.currentTimeMillis()
            ),
            LiveStudentExamProgress(
                studentId = "std_2",
                studentName = "Dewi Lestari",
                nim = "202400102",
                answeredCount = 8,
                totalQuestions = 10,
                remainingSeconds = 2740,
                isOnline = true,
                securityLevel = SecurityLevel.GREEN,
                violationScore = 20,
                category = ViolationCategory.NORMAL,
                lastActiveTimestamp = System.currentTimeMillis() - 5000
            ),
            LiveStudentExamProgress(
                studentId = "std_3",
                studentName = "Rian Saputra",
                nim = "202400103",
                answeredCount = 3,
                totalQuestions = 10,
                remainingSeconds = 3100,
                isOnline = true,
                securityLevel = SecurityLevel.YELLOW,
                violationScore = 60,
                category = ViolationCategory.MENCURIGAKAN,
                lastActiveTimestamp = System.currentTimeMillis() - 15000
            ),
            LiveStudentExamProgress(
                studentId = "std_4",
                studentName = "Ahmad Ridwan",
                nim = "202400104",
                answeredCount = 9,
                totalQuestions = 10,
                remainingSeconds = 1800,
                isOnline = false,
                securityLevel = SecurityLevel.YELLOW,
                violationScore = 35,
                category = ViolationCategory.PERLU_DIPERIKSA,
                lastActiveTimestamp = System.currentTimeMillis() - 120000
            )
        )
    }
}
