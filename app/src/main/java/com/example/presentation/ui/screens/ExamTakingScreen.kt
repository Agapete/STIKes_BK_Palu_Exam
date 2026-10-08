package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.QuestionOption
import com.example.data.model.QuestionType
import com.example.data.model.StudentAnswer
import com.example.presentation.ui.components.ViolationBadge
import com.example.presentation.viewmodel.ExamViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTakingScreen(viewModel: ExamViewModel, exam: Exam) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val formattedTime by viewModel.timerManager.formattedTime.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isPaletteOpen by viewModel.isQuestionPaletteOpen.collectAsState()
    val isSubmitConfirmOpen by viewModel.isSubmitConfirmationVisible.collectAsState()

    // Anti-cheating states
    val alertMessage by viewModel.antiCheatingEngine.activeAlert.collectAsState()
    val violationScore by viewModel.antiCheatingEngine.totalScore.collectAsState()
    val violationCategory by viewModel.antiCheatingEngine.category.collectAsState()

    val currentQuestion = questions.getOrNull(currentIndex)
    val currentAnswer = currentQuestion?.let { answers[it.id] }

    // Count statistics
    val answeredCount = questions.count { q ->
        val ans = answers[q.id]
        (q.type == QuestionType.MULTIPLE_CHOICE && !ans?.selectedOptionId.isNullOrBlank()) ||
                (q.type == QuestionType.ESSAY && !ans?.essayText.isNullOrBlank())
    }
    val unansweredCount = (questions.size - answeredCount).coerceAtLeast(0)

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Offline banner if disconnected
                    if (!isOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarningYellow)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Mode Offline Aktif - Jawaban tetap tersimpan lokal & sinkron otomatis",
                                color = Slate900,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(exam.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Soal ${currentIndex + 1} dari ${questions.size}",
                                    fontSize = 12.sp,
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tersimpan", fontSize = 11.sp, color = SuccessGreen)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Server Countdown Timer
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(formattedTime, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                                }
                            }

                            // Palette Sheet Button
                            IconButton(onClick = { viewModel.toggleQuestionPalette(true) }) {
                                Icon(Icons.Default.GridView, contentDescription = "Daftar Soal", tint = NavyPrimary)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flag / Ragu-ragu button
                    TextButton(
                        onClick = {
                            currentQuestion?.let { viewModel.toggleFlagQuestion(it.id) }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentAnswer?.isFlagged == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (currentAnswer?.isFlagged == true) WarningYellow else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentAnswer?.isFlagged == true) "Ragu-ragu" else "Tandai",
                            color = if (currentAnswer?.isFlagged == true) WarningYellow else Slate500,
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Previous button
                        OutlinedButton(
                            onClick = { viewModel.prevQuestion() },
                            enabled = currentIndex > 0 && exam.allowBackNavigation,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Sebelumnya")
                        }

                        // Next or Submit button
                        if (currentIndex < questions.size - 1) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Berikutnya")
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.showSubmitConfirmation(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kumpulkan")
                            }
                        }
                    }
                }
            }
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (currentQuestion != null) {
                // Category & Points row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentQuestion.category} • ${if (currentQuestion.type == QuestionType.MULTIPLE_CHOICE) "Pilihan Ganda" else "Essay"}",
                            color = NavyPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("Bobot: ${currentQuestion.maxScore.toInt()} Poin", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Text
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentQuestion.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate900,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options for Multiple Choice
                if (currentQuestion.type == QuestionType.MULTIPLE_CHOICE) {
                    currentQuestion.options.forEach { option ->
                        val isSelected = currentAnswer?.selectedOptionId == option.id
                        OptionCard(
                            option = option,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.answerMultipleChoice(currentQuestion.id, option.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    // Essay Input
                    var textInput by remember(currentQuestion.id) {
                        mutableStateOf(currentAnswer?.essayText ?: "")
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jawaban Essay Anda:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = {
                                    textInput = it
                                    viewModel.answerEssay(currentQuestion.id, it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 180.dp),
                                placeholder = { Text("Ketikkan uraian jawaban Anda di sini secara lengkap...") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Autosave otomatis aktif", fontSize = 11.sp, color = SuccessGreen)
                                Text("${textInput.length} karakter", fontSize = 11.sp, color = Slate400)
                            }
                        }
                    }
                }
            }
        }
    }

    // Question Palette Bottom Sheet
    if (isPaletteOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleQuestionPalette(false) },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Navigasi Soal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("Sudah dijawab: $answeredCount/${questions.size}", fontSize = 13.sp, color = Slate500)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(questions) { index, q ->
                        val ans = answers[q.id]
                        val isAnswered = (q.type == QuestionType.MULTIPLE_CHOICE && !ans?.selectedOptionId.isNullOrBlank()) ||
                                (q.type == QuestionType.ESSAY && !ans?.essayText.isNullOrBlank())
                        val isFlagged = ans?.isFlagged == true
                        val isCurrent = index == currentIndex

                        val bgColor = when {
                            isFlagged -> WarningYellow
                            isAnswered -> SuccessGreen
                            else -> Slate200
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isCurrent) 3.dp else 0.dp,
                                    color = if (isCurrent) Navy900 else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.goToQuestion(index)
                                    viewModel.toggleQuestionPalette(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAnswered || isFlagged) Color.White else Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(SuccessGreen, "Dijawab")
                    LegendItem(WarningYellow, "Ragu-ragu")
                    LegendItem(Slate300, "Belum")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.toggleQuestionPalette(false)
                        viewModel.showSubmitConfirmation(true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Kumpulkan Jawaban Ujian", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (isSubmitConfirmOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.showSubmitConfirmation(false) },
            title = { Text("Konfirmasi Kumpulkan Ujian", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ringkasan pengerjaan Anda:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Soal terjawab: $answeredCount dari ${questions.size}", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    if (unansweredCount > 0) {
                        Text("• Soal belum dijawab: $unansweredCount soal!", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                    Text("• Sisa waktu: $formattedTime", color = Slate700)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Setelah dikumpulkan, sesi ujian akan ditutup dan Anda tidak dapat mengubah jawaban kembali.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitExamFinal() },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Ya, Kumpulkan Final")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.showSubmitConfirmation(false) }) {
                    Text("Lanjut Mengerjakan")
                }
            }
        )
    }

    // Anti-Cheating Alert Dialog
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.antiCheatingEngine.dismissAlert() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed) },
            title = { Text("Peringatan Sistem Integritas", fontWeight = FontWeight.Bold, color = DangerRed) },
            text = {
                Column {
                    Text(alertMessage ?: "", fontSize = 14.sp, color = Slate900)
                    Spacer(modifier = Modifier.height(12.dp))
                    ViolationBadge(violationCategory, violationScore)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Seluruh aktivitas terekam dalam log audit dosen. Harap tetap berada pada aplikasi ujian.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.antiCheatingEngine.dismissAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Saya Mengerti & Kembali ke Soal")
                }
            }
        )
    }
}

@Composable
private fun OptionCard(
    option: QuestionOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NavyPrimary else Slate200,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.06f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) NavyPrimary else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Slate700
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = option.text,
                fontSize = 15.sp,
                color = Slate900,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Slate700)
    }
}

val Slate300 = Color(0xFFCBD5E1)
