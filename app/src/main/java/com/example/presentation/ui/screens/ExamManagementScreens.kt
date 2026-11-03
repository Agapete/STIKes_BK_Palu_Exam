package com.example.presentation.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.ExamStatus
import com.example.presentation.ui.components.SecurityLevelBadge
import com.example.presentation.viewmodel.ExamViewModel
import com.example.presentation.viewmodel.ScreenDestination
import com.example.ui.theme.*

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(viewModel: ExamViewModel) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.StudentDashboard)
    }

    LaunchedEffect(Unit) {
        viewModel.loadExams()
    }

    var selectedTab by remember { mutableStateOf(0) }
    val exams by viewModel.exams.collectAsState()

    val activeCount = exams.count { it.status == ExamStatus.ACTIVE }
    val scheduledCount = exams.count { it.status == ExamStatus.SCHEDULED }
    val endedCount = exams.count { it.status == ExamStatus.ENDED }

    val tabs = listOf(
        "Aktif ($activeCount)",
        "Mendatang ($scheduledCount)",
        "Riwayat ($endedCount)"
    )

    val filteredExams = when (selectedTab) {
        0 -> exams.filter { it.status == ExamStatus.ACTIVE }
        1 -> exams.filter { it.status == ExamStatus.SCHEDULED }
        else -> exams.filter { it.status == ExamStatus.ENDED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Ujian Mahasiswa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.StudentDashboard) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadExams() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang")
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = NavyPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (filteredExams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada ujian pada kategori ini",
                            color = Slate600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExams) { exam ->
                        ExamCardItem(exam = exam) {
                            viewModel.selectExam(exam)
                            viewModel.navigateTo(ScreenDestination.ExamDetail(exam))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCardItem(exam: Exam, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(exam.courseCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (exam.status) {
                                ExamStatus.ACTIVE -> SuccessGreenBg
                                ExamStatus.SCHEDULED -> SkyLight.copy(alpha = 0.2f)
                                else -> Slate200
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (exam.status) {
                            ExamStatus.ACTIVE -> "AKTIF"
                            ExamStatus.SCHEDULED -> "MENDATANG"
                            else -> "SELESAI"
                        },
                        color = when (exam.status) {
                            ExamStatus.ACTIVE -> SuccessGreen
                            ExamStatus.SCHEDULED -> NavyPrimary
                            else -> Slate700
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(exam.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(exam.lecturerName, fontSize = 13.sp, color = Slate500, modifier = Modifier.padding(top = 2.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${exam.durationMinutes} Menit", fontSize = 12.sp, color = Slate700)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpCenter, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${exam.questionCount} Soal", fontSize = 12.sp, color = Slate700)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lockdown", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(viewModel: ExamViewModel, exam: Exam) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.ExamList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Ujian", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.ExamList) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.ExamInstruction(exam)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lanjut ke Lembar Persetujuan & Aturan", fontWeight = FontWeight.Bold)
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
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(exam.courseCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exam.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(exam.courseName, fontSize = 14.sp, color = Slate700)
                    Text("Dosen: ${exam.lecturerName}", fontSize = 13.sp, color = Slate500, modifier = Modifier.padding(top = 2.dp))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Slate100)

                    DetailRow("Durasi Ujian", "${exam.durationMinutes} Menit")
                    DetailRow("Jumlah Soal", "${exam.questionCount} Soal (Pilihan Ganda & Essay)")
                    DetailRow("Passing Grade", "${exam.passingGrade} / 100")
                    DetailRow("Navigasi Bebas", if (exam.allowBackNavigation) "Diizinkan (Bisa kembali ke soal sebelumnya)" else "Satu Arah (Tidak bisa kembali)")
                    DetailRow("Mode Anti-Cheating", "Aktif (Lockdown Mode & Rekam Pelanggaran)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security warning card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarningYellowBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Peringatan Mode Terkunci", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Setelah ujian dimulai, HP Anda akan masuk ke Mode Ujian Terkunci (Fullscreen Sticky & FLAG_SECURE). Segala upaya beralih aplikasi, split screen, screenshot, atau manipulasi waktu akan dicatat dan mempengaruhi nilai akhir.",
                            fontSize = 12.sp,
                            color = Slate700,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Slate500)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamInstructionScreen(viewModel: ExamViewModel, exam: Exam) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.ExamDetail(exam))
    }

    val context = LocalContext.current
    val activity = context as? Activity
    var isAgreed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aturan & Lembar Persetujuan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.ExamDetail(exam)) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                viewModel.startExam(activity, exam)
                            }
                        },
                        enabled = isAgreed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            disabledContainerColor = Slate200
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aktifkan Lockdown & Mulai Ujian", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                .padding(20.dp)
        ) {
            Text(
                text = "Pernyataan Integritas Akademik",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    InstructionPoint("1", "Dilarang meminimalkan aplikasi, menekan tombol Home, atau berpindah aplikasi lain.")
                    InstructionPoint("2", "Dilarang menarik notification shade atau mengaktifkan split-screen / floating windows.")
                    InstructionPoint("3", "Tangkapan layar (screenshot) dan perekaman layar otomatis diblokir sistem.")
                    InstructionPoint("4", "Timer ujian dikontrol sepenuhnya oleh server. Perubahan jam lokal tidak akan menambah durasi.")
                    InstructionPoint("5", "Jawaban disimpan otomatis (autosave) secara lokal dan disinkronkan ke server secara berkala.")
                    InstructionPoint("6", "Bila internet terputus, tetap lanjutkan pengerjaan (mode offline aman).")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Agreement checkbox
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAgreed = !isAgreed }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreed,
                        onCheckedChange = { isAgreed = it },
                        colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saya telah membaca, memahami, dan menyetujui seluruh aturan di atas. Saya bersedia menerima sanksi akademik apabila terindikasi melakukan kecurangan.",
                        fontSize = 12.sp,
                        color = Slate900,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionPoint(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(NavyPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, color = Slate700, lineHeight = 19.sp, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    viewModel: ExamViewModel,
    exam: Exam,
    score: Double,
    total: Double,
    grade: String
) {
    val percentage = (score / total * 100).toInt()
    val isPassed = score >= exam.passingGrade

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Ujian", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.StudentDashboard) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Kembali ke Dashboard Mahasiswa", fontWeight = FontWeight.Bold)
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Icon Pass / Fail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) SuccessGreenBg else DangerRedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isPassed) SuccessGreen else DangerRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPassed) "Ujian Berhasil Diserahkan!" else "Ujian Berhasil Diserahkan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Text(
                text = exam.title,
                fontSize = 14.sp,
                color = Slate500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Score Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NILAI AKHIR SEMENTARA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "%.1f".format(score),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) SuccessGreen else DangerRed
                    )

                    Text(
                        text = "Grade: $grade ($percentage%)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Slate100)

                    DetailRow("Status Kelulusan", if (isPassed) "LULUS (>= ${exam.passingGrade})" else "BELUM LULUS")
                    DetailRow("Passing Grade Standar", "${exam.passingGrade} / 100")
                    DetailRow("Status Soal Essay", "Menunggu Verifikasi & Penilaian Dosen")
                    DetailRow("Skor Pelanggaran Integritas", "0 Poin (Normal / Bersih)")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(viewModel: ExamViewModel) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.StudentDashboard)
    }

    val user by viewModel.currentUser.collectAsState()
    val report by viewModel.deviceReport.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Perangkat BYOD", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.StudentDashboard) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("DATA IDENTITAS MAHASISWA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow("Nama Lengkap", user.fullName)
                    DetailRow("NIM", user.identifier)
                    DetailRow("Program Studi", user.studyProgram)
                    DetailRow("Kelas", user.className)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STATUS PERANGKAT BYOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        SecurityLevelBadge(report.securityLevel)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow("Tipe Smartphone", report.deviceModel)
                    DetailRow("Sistem Operasi", report.androidVersion)
                    DetailRow("Izin FLAG_SECURE", "Aktif (Screenshot Diblokir)")
                    DetailRow("Status Root", if (report.isRooted) "Terdeteksi Root" else "Normal")
                    DetailRow("Mode Pengembang", if (report.isDebugMode) "USB Debugging Aktif" else "Nonaktif")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(ScreenDestination.Login) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun (Logout)")
            }
        }
    }
}
