package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.presentation.ui.screens.*
import com.example.presentation.viewmodel.ExamViewModel
import com.example.presentation.viewmodel.ScreenDestination
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    when (val screen = currentScreen) {
                        is ScreenDestination.Splash -> SplashScreen(viewModel)
                        is ScreenDestination.WelcomeSecurityCheck -> WelcomeSecurityScreen(viewModel)
                        is ScreenDestination.Login -> LoginScreen(viewModel)
                        is ScreenDestination.StudentDashboard -> StudentDashboardScreen(viewModel)
                        is ScreenDestination.ExamList -> ExamListScreen(viewModel)
                        is ScreenDestination.ExamDetail -> ExamDetailScreen(viewModel, screen.exam)
                        is ScreenDestination.ExamInstruction -> ExamInstructionScreen(viewModel, screen.exam)
                        is ScreenDestination.ExamTaking -> ExamTakingScreen(viewModel, screen.exam)
                        is ScreenDestination.ExamResult -> ExamResultScreen(
                            viewModel = viewModel,
                            exam = screen.exam,
                            score = screen.score,
                            total = screen.total,
                            grade = screen.grade
                        )
                        is ScreenDestination.StudentProfile -> StudentProfileScreen(viewModel)
                        is ScreenDestination.LecturerDashboard -> LecturerDashboardScreen(viewModel)
                        else -> StudentDashboardScreen(viewModel)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.currentScreen.value is ScreenDestination.ExamTaking) {
            viewModel.antiCheatingEngine.recordAppBackground()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentScreen.value is ScreenDestination.ExamTaking) {
            viewModel.antiCheatingEngine.recordAppForeground()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && viewModel.currentScreen.value is ScreenDestination.ExamTaking) {
            viewModel.antiCheatingEngine.recordWindowFocusLost()
        }
    }
}

/**
 * Kept for Robolectric and Roborazzi screenshot test compatibility
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("CampusExam") }
}
