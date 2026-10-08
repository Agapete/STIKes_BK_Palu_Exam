package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.presentation.ui.components.SecurityLevelBadge
import com.example.presentation.ui.components.ViolationBadge
import com.example.presentation.viewmodel.ExamViewModel
import com.example.presentation.viewmodel.ScreenDestination
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDashboardScreen(viewModel: ExamViewModel) {
    val activeTab by viewModel.lecturerActiveTab.collectAsState()
    val tabs = listOf("Monitoring Live", "Pusat Pelanggaran", "Bank Soal", "Import G-Form")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard Dosen & Pengawas", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("UTS Algoritma & Struktur Data (TI-3A)", fontSize = 12.sp, color = Slate500)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.switchUserRole(UserRole.STUDENT) }) {
                        Text("Kembali ke Mahasiswa", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = NavyPrimary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { viewModel.setLecturerActiveTab(index) },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (activeTab) {
                0 -> LiveMonitoringTab(viewModel)
                1 -> ViolationCenterTab(viewModel)
                2 -> QuestionBankTab(viewModel)
                3 -> GoogleFormImportTab(viewModel)
            }
        }
    }
}

@Composable
private fun LiveMonitoringTab(viewModel: ExamViewModel) {
    val liveStudents by viewModel.liveStudents.collectAsState()
    var selectedStudent by remember { mutableStateOf<LiveStudentExamProgress?>(null) }
    var actionDialogMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiveMiniStat("Total Peserta", "${liveStudents.size} Mahasiswa", Icons.Default.Group, NavyPrimary, Modifier.weight(1f))
                LiveMiniStat("Sedang Online", "${liveStudents.count { it.isOnline }} Aktif", Icons.Default.Wifi, SuccessGreen, Modifier.weight(1f))
                LiveMiniStat("Mencurigakan", "${liveStudents.count { it.category != ViolationCategory.NORMAL }} Siswa", Icons.Default.Warning, AlertOrange, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Daftar Mahasiswa yang Sedang Mengikuti Ujian", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(liveStudents) { student ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(student.studentName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text("NIM: ${student.nim}", fontSize = 12.sp, color = Slate500)
                        }

                        // Online / Offline chip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (student.isOnline) SuccessGreen else DangerRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (student.isOnline) "Online" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (student.isOnline) SuccessGreen else DangerRed
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Progres Soal", fontSize = 11.sp, color = Slate400)
                            Text("${student.answeredCount} / ${student.totalQuestions} Dijawab", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }

                        Column {
                            Text("Sisa Waktu", fontSize = 11.sp, color = Slate400)
                            Text("${student.remainingSeconds / 60} Menit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }

                        Column {
                            Text("Status Integritas", fontSize = 11.sp, color = Slate400)
                            ViolationBadge(student.category, student.violationScore)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                actionDialogMessage = "Peringatan terkirim ke HP ${student.studentName}: 'Harap tetap fokus pada soal!'"
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Peringatkan", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                actionDialogMessage = "Sesi ujian ${student.studentName} dipulihkan / token direset oleh Dosen Pengawas."
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Pulihkan", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (actionDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { actionDialogMessage = null },
            title = { Text("Aksi Pengawas Dosen Berhasil", fontWeight = FontWeight.Bold) },
            text = { Text(actionDialogMessage ?: "") },
            confirmButton = {
                Button(onClick = { actionDialogMessage = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun ViolationCenterTab(viewModel: ExamViewModel) {
    val liveStudents by viewModel.liveStudents.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Pusat Pengawasan Pelanggaran Anti-Cheating", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text(
            "Mengklasifikasikan insiden secara objektif untuk peninjauan pengawas.",
            fontSize = 12.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Category breakdown overview
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Klasifikasi Skor Pelanggaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
                Spacer(modifier = Modifier.height(10.dp))
                ViolationSummaryRow("NORMAL (0 - 20 Poin)", "Tidak ada indikasi curang", SuccessGreen)
                ViolationSummaryRow("PERLU DIPERIKSA (21 - 50 Poin)", "1-2 kali perpindahan aplikasi atau putus koneksi", WarningYellow)
                ViolationSummaryRow("MENCURIGAKAN (51 - 80 Poin)", "Berulang kali keluar aplikasi atau kehilangan fokus", AlertOrange)
                ViolationSummaryRow("PELANGGARAN BERAT (81+ Poin)", "Percobaan manipulasi intensif / pembatalan ujian", DangerRed)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Log Insiden Terkini", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(8.dp))

        // Sample Incident Logs
        IncidentLogCard("Rian Saputra", "202400103", "APP_BACKGROUND", "Aplikasi diminimalkan selama 18 detik", "+20", AlertOrange)
        Spacer(modifier = Modifier.height(8.dp))
        IncidentLogCard("Rian Saputra", "202400103", "WINDOW_FOCUS_LOST", "Notification panel dibuka", "+30", AlertOrange)
        Spacer(modifier = Modifier.height(8.dp))
        IncidentLogCard("Ahmad Ridwan", "202400104", "NETWORK_DISCONNECTED", "Koneksi terputus, beralih ke Room cache", "+5", WarningYellow)
        Spacer(modifier = Modifier.height(8.dp))
        IncidentLogCard("Dewi Lestari", "202400102", "SCREEN_CAPTURE_ATTEMPT", "Percobaan screenshot diblokir oleh FLAG_SECURE", "+30", DangerRed)
    }
}

@Composable
private fun IncidentLogCard(
    studentName: String,
    nim: String,
    eventType: String,
    detail: String,
    points: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(studentName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("($nim)", fontSize = 12.sp, color = Slate400)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(eventType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
                Text(detail, fontSize = 12.sp, color = Slate600)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(points, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun QuestionBankTab(viewModel: ExamViewModel) {
    val bankQuestions by viewModel.questionBank.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bank Soal Dosen", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("Total ${bankQuestions.size} soal tersimpan", fontSize = 12.sp, color = Slate500)
                }

                Button(
                    onClick = { viewModel.setLecturerActiveTab(3) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import G-Form", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(bankQuestions) { q ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(q.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Text(
                            if (q.type == QuestionType.MULTIPLE_CHOICE) "Pilihan Ganda" else "Essay",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(q.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate900)

                    if (q.type == QuestionType.MULTIPLE_CHOICE && q.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        q.options.forEach { opt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${opt.label}. ${opt.text}",
                                    fontSize = 12.sp,
                                    color = if (opt.isCorrect) SuccessGreen else Slate600,
                                    fontWeight = if (opt.isCorrect) FontWeight.Bold else FontWeight.Normal
                                )
                                if (opt.isCorrect) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleFormImportTab(viewModel: ExamViewModel) {
    val formUrl by viewModel.googleFormUrl.collectAsState()
    val previewQuestions by viewModel.googleFormPreviewQuestions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Import Soal dari Google Form", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text(
            "Masukkan tautan kuis/formulir Google Form untuk mengimpor soal pilihan ganda dan essay secara otomatis ke dalam bank soal.",
            fontSize = 13.sp,
            color = Slate500,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("URL Google Form / ID Formulir", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = formUrl,
                    onValueChange = { viewModel.setGoogleFormUrl(it) },
                    placeholder = { Text("https://docs.google.com/forms/d/e/1FAIpQLSc...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.previewGoogleForm() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parsing & Preview Soal")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.setGoogleFormUrl("https://docs.google.com/forms/d/e/1FAIpQLScJarkomExam2026/viewform")
                            viewModel.previewGoogleForm()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pakai Contoh URL")
                    }
                }
            }
        }

        // Preview Section
        if (previewQuestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hasil Parsing (${previewQuestions.size} Soal Terdeteksi)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)

                Button(
                    onClick = { viewModel.importGoogleFormQuestions() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan ke Bank Soal")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            previewQuestions.forEach { q ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Soal #${q.number} • ${if (q.type == QuestionType.MULTIPLE_CHOICE) "Pilihan Ganda" else "Essay"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text("Bobot: ${q.maxScore.toInt()} Poin", fontSize = 12.sp, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.text, fontSize = 14.sp, color = Slate900)

                        if (q.options.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            q.options.forEach { opt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${opt.label}. ${opt.text}",
                                        fontSize = 12.sp,
                                        color = if (opt.isCorrect) SuccessGreen else Slate600,
                                        fontWeight = if (opt.isCorrect) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (opt.isCorrect) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(Kunci Jawaban)", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveMiniStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(label, fontSize = 10.sp, color = Slate400)
        }
    }
}

@Composable
private fun ViolationSummaryRow(title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Text(desc, fontSize = 11.sp, color = Slate500)
        }
    }
}
