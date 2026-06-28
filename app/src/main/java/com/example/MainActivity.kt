package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ActiveLessonScreen
import com.example.ui.screens.LessonSummaryScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AcademyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AcademyViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (viewModel.currentScreen) {
                        AppScreen.ONBOARDING -> OnboardingScreen(viewModel)
                        AppScreen.MAIN_DASHBOARD -> MainDashboardScreen(viewModel)
                        AppScreen.ACTIVE_LESSON -> ActiveLessonScreen(viewModel)
                        AppScreen.LESSON_SUMMARY -> LessonSummaryScreen(viewModel)
                    }
                }
            }
        }
    }
}
