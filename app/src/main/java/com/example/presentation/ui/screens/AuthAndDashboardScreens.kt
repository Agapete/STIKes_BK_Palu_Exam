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
    var username by remember { mutableStateOf("202400101") }
    var password by remember { mutableStateOf("PasswordMhs123!") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    val report by viewModel.deviceReport.collectAsState()

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

            Text("Portal STIKES BK Palu Exam", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(
                "STIKes BK Palu",
                fontSize = 13.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Role Switcher Segmented Control
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate200.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RolePill("Mahasiswa", selectedRole == UserRole.STUDENT, Modifier.weight(1f)) {
                        selectedRole = UserRole.STUDENT
                        username = "202400101"
                    }
                    RolePill("Dosen", selectedRole == UserRole.LECTURER, Modifier.weight(1f)) {
                        selectedRole = UserRole.LECTURER
                        username = "19800512"
                    }
                    RolePill("Admin", selectedRole == UserRole.ADMIN, Modifier.weight(1f)) {
                        selectedRole = UserRole.ADMIN
                        username = "admin_puskom"
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (selectedRole == UserRole.STUDENT) "Nomor Induk Mahasiswa (NIM)" else "NIP / Username",
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
                        shape = RoundedCornerShape(12.dp)
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
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.switchUserRole(selectedRole)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text(
                            text = if (selectedRole == UserRole.STUDENT) "Masuk Ujian Mahasiswa" else "Buka Dashboard Pengawas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
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
    val exams = viewModel.exams

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
                    Text("NIM: ${user.identifier} • ${user.className}", fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
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

            // Role Switcher Quick Pill (for review and testing)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SkyLight.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mode Dosen & Admin Console", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    }
                    TextButton(
                        onClick = { viewModel.switchUserRole(UserRole.LECTURER) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Buka Console", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Mendatang",
                    value = exams.count { it.status == ExamStatus.SCHEDULED }.toString(),
                    subtitle = "Jadwal Minggu Ini",
                    icon = Icons.Default.Event,
                    accentColor = BlueAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Exam Highlight Card
            val activeExam = exams.find { it.status == ExamStatus.ACTIVE }
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
