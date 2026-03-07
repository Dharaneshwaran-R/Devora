package com.devora.devicemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devora.devicemanager.ui.navigation.AppNavigation
import com.devora.devicemanager.ui.theme.DevoraTheme
import com.devora.devicemanager.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVm: ThemeViewModel = viewModel()
            val isDark = themeVm.isDark

            DevoraTheme(isDark = isDark) {
                // Using enableEdgeToEdge and system bars handling in modern Compose
                AppNavigation(
                    isDark = isDark,
                    onThemeToggle = themeVm::toggle
                )
            }
        }
    }
}