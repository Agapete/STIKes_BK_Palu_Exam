package com.example.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.ui.components.SecurityLevelBadge
import com.example.presentation.viewmodel.ExamViewModel
import com.example.presentation.viewmodel.ScreenDestination
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: ExamViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(2200)
        viewModel.navigateTo(ScreenDestination.WelcomeSecurityCheck)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Navy900, Navy800, NavyPrimaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(2.dp, SkyLight.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Logo",
                    tint = SkyLight,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CampusExam Lockdown",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Platform Ujian BYOD Mahasiswa\ndengan Anti-Cheating & Integrity Monitoring",
                fontSize = 14.sp,
                color = Slate400,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = SkyLight,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Memeriksa integritas sistem & status perangkat...",
                fontSize = 12.sp,
                color = Slate400
            )
        }
    }
}

@Composable
fun WelcomeSecurityScreen(viewModel: ExamViewModel) {
    val report by viewModel.deviceReport.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.refreshDeviceCheck() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cek Ulang")
                    }

                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.Login) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lanjut ke Login", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Slate50)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Pemeriksaan Keamanan Perangkat",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Text(
                text = "Sebelum memulai ujian, aplikasi memastikan HP pribadi (BYOD) Anda memenuhi standar integritas akademik.",
                fontSize = 13.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
                lineHeight = 19.sp
            )

            // Security Status Card
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
                        Column {
                            Text("STATUS INTEGRITAS", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(report.deviceModel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        SecurityLevelBadge(report.securityLevel)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Slate100)

                    // Checklist items
                    SecurityCheckRow(
                        title = "Versi Sistem Operasi",
                        value = report.androidVersion,
                        isPass = true
                    )
                    SecurityCheckRow(
                        title = "Status Root / Magisk / Su",
                        value = if (report.isRooted) "Terdeteksi Root (Bahaya)" else "Clean (Tidak Ter-root)",
                        isPass = !report.isRooted
                    )
                    SecurityCheckRow(
                        title = "USB Debugging / Developer",
                        value = if (report.isDebugMode) "Aktif (Disarankan nonaktif)" else "Nonaktif",
                        isPass = !report.isDebugMode
                    )
                    SecurityCheckRow(
                        title = "Koneksi Jaringan Ujian",
                        value = if (report.isNetworkSecure) "Terhubung Online" else "Offline",
                        isPass = report.isNetworkSecure
                    )
                    SecurityCheckRow(
                        title = "Kapabilitas Layar Terkunci",
                        value = if (report.isLockTaskSupported) "Mendukung Kiosk Pinning" else "Terbatas",
                        isPass = report.isLockTaskSupported
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Privacy Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kebijakan Privasi Mahasiswa (BYOD)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Sistem TIDAK mengakses kontak, foto pribadi, histori chat, atau file di luar aplikasi.\n" +
                                "• Sistem HANYA memantau apakah aplikasi tetap aktif (foreground) selama ujian.\n" +
                                "• Tangkapan layar otomatis diblokir oleh sistem demi keamanan naskah soal.\n" +
                                "• Log aktivitas hanya disimpan selama periode audit penilaian oleh dosen.",
                        fontSize = 12.sp,
                        color = Slate700,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityCheckRow(title: String, value: String, isPass: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isPass) SuccessGreen else WarningYellow,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, color = Slate500)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
    }
}
