package com.devora.devicemanager.ui.navigation

import android.content.Intent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devora.devicemanager.MainActivity
import com.devora.devicemanager.ui.screens.dashboard.DashboardScreen
import com.devora.devicemanager.ui.screens.devices.DeviceDetailScreen
import com.devora.devicemanager.ui.screens.devices.DeviceListScreen
import com.devora.devicemanager.ui.screens.employee.EmployeeRegisterScreen
import com.devora.devicemanager.ui.screens.employeedashboard.EmployeeDashboardScreen
import com.devora.devicemanager.ui.screens.enrollment.AdminGenerateEnrollmentScreen
import com.devora.devicemanager.ui.screens.enrollment.EmployeeEnrollmentScreen
import com.devora.devicemanager.ui.screens.login.LoginScreen
import com.devora.devicemanager.ui.screens.settings.SettingsScreen
import com.devora.devicemanager.ui.screens.splash.SplashScreen
import com.devora.devicemanager.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Bottom nav helper — preserves back stack state for tab switching
    val navigateTo: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
        },
        exitTransition = {
            fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
        },
        popExitTransition = {
            fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 }
        }
    ) {

        // ═══════════════════════════════════
        // SPLASH
        // ═══════════════════════════════════
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // ═══════════════════════════════════
        // LOGIN
        // ═══════════════════════════════════
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    authViewModel.loginAdmin()
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onEmployeeRegister = {
                    navController.navigate("employee_register")
                },
                onEmployeeEnroll = {
                    navController.navigate("employee_enrollment")
                },
                isDark = isDark,
                onThemeToggle = onThemeToggle
            )
        }

        // ═══════════════════════════════════
        // EMPLOYEE REGISTRATION
        // ═══════════════════════════════════
        composable("employee_register") {
            EmployeeRegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate("login") {
                        popUpTo("employee_register") { inclusive = true }
                    }
                },
                isDark = isDark
            )
        }

        // ═══════════════════════════════════
        // ADMIN ROUTES
        // ═══════════════════════════════════

        // Dashboard (admin home)
        composable("dashboard") {
            DashboardScreen(
                onNavigate = navigateTo,
                isDark = isDark,
                onThemeToggle = onThemeToggle
            )
        }

        // Device List
        composable("device_list") {
            DeviceListScreen(
                onDeviceClick = { deviceName ->
                    navController.navigate("device_detail/$deviceName")
                },
                onEnrollClick = {
                    navController.navigate("admin_generate_enrollment")
                },
                onNavigate = navigateTo,
                isDark = isDark
            )
        }

        // Device Detail
        composable("device_detail/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DeviceDetailScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                isDark = isDark
            )
        }

        // Admin Generate Enrollment (QR/Token)
        composable("admin_generate_enrollment") {
            AdminGenerateEnrollmentScreen(
                onBack = { navController.popBackStack() },
                isDark = isDark
            )
        }

        // Settings
        composable("settings") {
            val context = LocalContext.current
            SettingsScreen(
                isDark = isDark,
                onThemeToggle = onThemeToggle,
                onSignOut = {
                    authViewModel.signOut()
                    // Restart activity to go back to login
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
                onNavigate = navigateTo
            )
        }

        // ═══════════════════════════════════
        // EMPLOYEE ROUTES
        // ═══════════════════════════════════

        // Employee Enrollment (QR Scan / Token Input)
        composable("employee_enrollment") {
            EmployeeEnrollmentScreen(
                onEnrollSuccess = {
                    authViewModel.loginEmployee()
                    navController.navigate("employee_dashboard") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Employee Dashboard (post-enrollment home)
        composable("employee_dashboard") {
            EmployeeDashboardScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isDark = isDark,
                onThemeToggle = onThemeToggle
            )
        }
    }
}


