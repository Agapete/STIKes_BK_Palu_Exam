package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamStatus
import com.example.data.model.UserRole
import com.example.presentation.ui.components.SecurityLevelBadge
import com.example.presentation.ui.components.StatCard
import com.example.presentation.viewmodel.ExamViewModel
import com.example.presentation.viewmodel.ScreenDestination
import com.example.ui.theme.*

@Composable
fun LoginScreen(viewModel: ExamViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }

    // Login Form State
    var username by remember { mutableStateOf("202400101") }
    var password by remember { mutableStateOf("stikesbk123") }

    // Register Form State
    var regFullName by remember { mutableStateOf("") }
    var regNim by remember { mutableStateOf("") }
    var regStudyProgram by remember { mutableStateOf("D3 Keperawatan") }
    var regClassName by remember { mutableStateOf("KLP-A") }

    val report by viewModel.deviceReport.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Title
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.logo_stikes),
                contentDescription = "Logo STIKes BK Palu",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isRegisterMode) "Pendaftaran Mahasiswa" else "Portal STIKES BK Palu Exam", 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold, 
                color = Slate900
            )
            Text(
                if (isRegisterMode) "Lengkapi identitas untuk akun ujian" else "STIKes BK Palu",
                fontSize = 13.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Input Fields Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isRegisterMode) {
                        // REGISTRATION FORM
                        Text("Nama Lengkap", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            placeholder = { Text("Contoh: Siti Aminah", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Nomor Induk Mahasiswa (NIM)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regNim,
                            onValueChange = { regNim = it },
                            placeholder = { Text("Contoh: 202400199", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Program Studi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regStudyProgram,
                            onValueChange = { regStudyProgram = it },
                            placeholder = { Text("D3 Keperawatan / S1 Administrasi Kesehatan", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Kelas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regClassName,
                            onValueChange = { regClassName = it },
                            placeholder = { Text("Contoh: KLP-A", color = Slate400) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regNim.isBlank() || regStudyProgram.isBlank() || regClassName.isBlank()) {
                                    android.widget.Toast.makeText(context, "Semua data pendaftaran wajib diisi!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.registerWithApi(regFullName, regNim, regStudyProgram, regClassName) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                    if (success) {
                                        username = regNim
                                        password = "stikesbk123"
                                        isRegisterMode = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Daftar Sekarang", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { isRegisterMode = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sudah punya akun? Masuk di sini", color = NavyPrimary, fontSize = 13.sp)
                        }

                    } else {
                        // LOGIN FORM
                        Text(
                            text = "Nomor Induk Mahasiswa (NIM)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate400) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Kata Sandi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900,
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate300
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    android.widget.Toast.makeText(context, "Username dan password tidak boleh kosong!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.loginWithApi(username, password) { success, msg ->
                                    if (!success) {
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text(
                                text = "Masuk Ujian Mahasiswa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { isRegisterMode = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Belum punya akun? Daftar di sini", color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Device security status footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Integritas Perangkat:", fontSize = 12.sp, color = Slate500)
                Spacer(modifier = Modifier.width(6.dp))
                SecurityLevelBadge(report.securityLevel)
            }
        }
    }
}

@Composable
private fun RolePill(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NavyPrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Slate700
        )
    }
}

@Composable
fun StudentDashboardScreen(viewModel: ExamViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val report by viewModel.deviceReport.collectAsState()
    val exams by viewModel.exams.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExams()
    }

    Scaffold(
        containerColor = Slate50,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(ScreenDestination.ExamList) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("Ujian") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(ScreenDestination.StudentProfile) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Selamat datang,", fontSize = 13.sp, color = Slate500)
                    Text(user.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("${user.studyProgram} • ${user.className} (NIM: ${user.identifier})", fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                }

                // Network simulation button
                OutlinedButton(
                    onClick = { viewModel.toggleNetworkSimulation() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isOnline) SuccessGreen else DangerRed
                    ),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) SuccessGreen else DangerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOnline) "Online" else "Offline (Simulasi)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))



            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Ujian Aktif",
                    value = exams.count { it.status == ExamStatus.ACTIVE }.toString(),
                    subtitle = "Siap Dikerjakan",
                    icon = Icons.Default.PlayCircle,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.ExamList) }
                )
                StatCard(
                    title = "Mendatang",
                    value = exams.count { it.status == ExamStatus.SCHEDULED }.toString(),
                    subtitle = "Jadwal Minggu Ini",
                    icon = Icons.Default.Event,
                    accentColor = BlueAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.ExamList) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Exam Highlight Card
            val activeExam = exams.find { it.status == ExamStatus.ACTIVE } ?: exams.firstOrNull()
            if (activeExam != null) {
                Text("Ujian Aktif Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(activeExam.courseCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("BERLANGSUNG", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(activeExam.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Text(activeExam.lecturerName, fontSize = 13.sp, color = Slate500, modifier = Modifier.padding(top = 2.dp))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${activeExam.durationMinutes} Menit", fontSize = 12.sp, color = Slate700)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${activeExam.questionCount} Soal (PG + Essay)", fontSize = 12.sp, color = Slate700)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.selectExam(activeExam)
                                viewModel.navigateTo(ScreenDestination.ExamDetail(activeExam))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Ujian & Masuk Ruang Ujian", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text("Ujian Aktif Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada ujian aktif yang tersinkronisasi.", fontSize = 13.sp, color = Slate700)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadExams() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Muat Ulang / Ambil Jadwal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Links
            Text("Aktivitas & Navigasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Daftar Seluruh Ujian", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Jadwal aktif, mendatang, dan arsip") },
                        leadingContent = {
                            Icon(Icons.Default.ListAlt, contentDescription = null, tint = NavyPrimary)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                        },
                        modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.ExamList) }
                    )
                    HorizontalDivider(color = Slate100)
                    ListItem(
                        headlineContent = { Text("Profil & Perangkat BYOD", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Detail mahasiswa dan status izin keamanan") },
                        leadingContent = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NavyPrimary)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                        },
                        modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.StudentProfile) }
                    )
                }
            }
        }
    }
}
