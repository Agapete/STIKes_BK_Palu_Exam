package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(
    val fullName: String,
    val username: String,
    val studyProgram: String,
    val className: String
)
data class LoginResponse(
    val status: String,
    val message: String?,
    val token: String?,
    val user: UserDto?
)
data class UserDto(
    val id: String, 
    val username: String, 
    val role: String, 
    val name: String,
    val studyProgram: String? = null,
    val className: String? = null
)

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse>

    @GET("/api/student/exams")
    suspend fun getExams(): Response<ExamsResponse>

    @POST("/api/student/exam/{examId}/start")
    suspend fun startExam(
        @retrofit2.http.Path("examId") examId: String
    ): Response<ExamStartResponse>

    @POST("/api/student/exam/{attemptId}/answer")
    suspend fun submitAnswer(
        @retrofit2.http.Path("attemptId") attemptId: String,
        @Body request: AnswerRequest
    ): Response<BaseResponse>

    @POST("/api/student/exam/{attemptId}/violation")
    suspend fun reportViolation(
        @retrofit2.http.Path("attemptId") attemptId: String,
        @Body request: ViolationRequest
    ): Response<BaseResponse>

    @POST("/api/student/exam/{attemptId}/submit")
    suspend fun submitExam(
        @retrofit2.http.Path("attemptId") attemptId: String
    ): Response<ExamSubmitResponse>
}

data class ExamSubmitResponse(
    val status: String,
    val message: String? = null,
    val data: ExamSubmitResultDto? = null
)

data class ExamSubmitResultDto(
    val score: Double,
    val penalty: Int,
    val finalScore: Double,
    val grade: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passed: Boolean
)

data class BaseResponse(val status: String, val message: String?)

data class ExamsResponse(val status: String, val data: List<ExamDto>)
data class ExamDto(
    val id: String, 
    val title: String, 
    val course: String, 
    val lecturer: String, 
    val durationMinutes: Int, 
    val startTime: String, 
    val endTime: String,
    val status: String? = "ACTIVE",
    val questionCount: Int? = 0
)

data class ExamStartResponse(
    val status: String, 
    val attemptId: String?, 
    val questions: List<QuestionDto>?
)
data class QuestionDto(
    val id: String, 
    val text: String, 
    val options: List<OptionDto>
)
data class OptionDto(val id: String, val label: String, val text: String)

data class AnswerRequest(val questionId: String, val optionId: String)
data class ViolationRequest(
    val eventType: String, 
    val severity: String, 
    val scoreIncrement: Int, 
    val metadata: String?
)
