package com.devora.devicemanager.ui.screens.devices

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devora.devicemanager.network.AppInventoryItem
import com.devora.devicemanager.network.DeviceResponse
import com.devora.devicemanager.network.RetrofitClient
import com.devora.devicemanager.ui.components.DevoraCard
import com.devora.devicemanager.ui.components.SectionHeader
import com.devora.devicemanager.ui.components.StatusBadge
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.BgSurface
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.DarkBgElevated
import com.devora.devicemanager.ui.theme.DarkBgSurface
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDeep
import com.devora.devicemanager.ui.theme.PurpleDim
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import com.devora.devicemanager.ui.theme.Warning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════
// DEVICE DETAIL SCREEN
// ══════════════════════════════════════

@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onBack: () -> Unit,
    isDark: Boolean
) {
    // Fetch real device data from API
    var deviceResponse by remember { mutableStateOf<DeviceResponse?>(null) }

    LaunchedEffect(deviceId) {
        try {
            val response = RetrofitClient.api.getDeviceList()
            if (response.isSuccessful) {
                deviceResponse = response.body()?.find { it.deviceId == deviceId }
            }
        } catch (e: Exception) {
            Log.e("DeviceDetail", "Failed to fetch device: ${e.message}")
        }
    }

    val device = if (deviceResponse != null) {
        val dr = deviceResponse!!
        val employeeName = dr.employeeName?.takeIf { it.isNotBlank() }
        val displayName = employeeName?.let { "$it's Device" } ?: (dr.deviceModel ?: "Unknown")
        val subtitle = "${dr.manufacturer.orEmpty()} ${dr.deviceModel.orEmpty()}".trim().let {
            if (it.isBlank()) "Unknown Device · ${dr.enrollmentMethod}" else "$it · ${dr.enrollmentMethod}"
        }
        Device(
            name = displayName,
            subtitle = subtitle,
            searchManufacturer = dr.manufacturer.orEmpty(),
            searchModel = dr.deviceModel.orEmpty(),
            status = when (dr.status.uppercase()) {
                "ACTIVE", "ENROLLED" -> "ONLINE"
                else -> "OFFLINE"
            },
            api = "ID: ${dr.id}",
            initial = displayName.take(1).uppercase(),
            deviceId = dr.deviceId,
            lastSeen = "Enrolled ${dr.enrolledAt.take(10)}"
        )
    } else {
        Device(
            name = deviceId.ifEmpty { "Unknown Device" },
            subtitle = "Loading...",
            searchManufacturer = "",
            searchModel = "",
            status = "PENDING",
            api = "—",
            initial = deviceId.take(1).uppercase().ifEmpty { "?" },
            deviceId = deviceId,
            lastSeen = "Unknown"
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("INFO", "APPS", "ACTIVITY", "ACTIONS")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showLockDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }
    var wipeStep by remember { mutableIntStateOf(0) }
    var wipeConfirmText by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }

    val bgColor = if (isDark) DarkBgBase else BgBase
    val textColor = if (isDark) DarkTextPrimary else TextPrimary
    val surfaceBg = if (isDark) DarkBgSurface else BgSurface

    Scaffold(
        containerColor = bgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ══════ TOP BAR ══════
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PurpleCore
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        device.name,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    Text(
                        device.subtitle,
                        fontFamily = DMSans,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                StatusBadge(status = device.status)
            }

            Spacer(Modifier.height(16.dp))

            // ══════ TAB ROW ══════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) DarkBgSurface else Color(0xFFE9EEF3))
                    .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PurpleCore else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab,
                            fontFamily = if (isSelected) PlusJakartaSans else DMSans,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // TAB CONTENT
            // ══════════════════════════════════════

            when (selectedTab) {
                0 -> InfoTab(device = device, deviceResponse = deviceResponse, isDark = isDark, textColor = textColor)
                1 -> AppsTab(deviceId = deviceId, isDark = isDark, textColor = textColor)
                2 -> ActivityTab(isDark = isDark, textColor = textColor)
                3 -> ActionsTab(
                    isDark = isDark,
                    textColor = textColor,
                    isSyncing = isSyncing,
                    onLock = { showLockDialog = true },
                    onPasswordReset = { showPasswordDialog = true },
                    onClearData = { showClearDataDialog = true },
                    onSync = {
                        coroutineScope.launch {
                            isSyncing = true
                            delay(2000)
                            isSyncing = false
                            snackbarHostState.showSnackbar("✓ Sync complete")
                        }
                    },
                    onWipe = {
                        wipeStep = 1
                        wipeConfirmText = ""
                        showWipeDialog = true
                    }
                )
            }
        }
    }

    // ══════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════

    // Lock Device Dialog
    if (showLockDialog) {
        ConfirmActionDialog(
            title = "Lock Device",
            message = "This will immediately lock the device screen. The employee will need to enter their PIN/password to unlock.",
            icon = Icons.Outlined.Lock,
            iconColor = PurpleCore,
            confirmText = "Lock Now",
            confirmColor = PurpleCore,
            isDark = isDark,
            onDismiss = { showLockDialog = false },
            onConfirm = {
                showLockDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✓ Device locked successfully")
                }
            }
        )
    }

    // Password Reset Dialog
    if (showPasswordDialog) {
        ConfirmActionDialog(
            title = "Force Password Reset",
            message = "The employee will be required to set a new password on their next unlock. Their current password will be invalidated.",
            icon = Icons.Outlined.Password,
            iconColor = Warning,
            confirmText = "Reset Password",
            confirmColor = Warning,
            isDark = isDark,
            onDismiss = { showPasswordDialog = false },
            onConfirm = {
                showPasswordDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✓ Password reset initiated")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        ConfirmActionDialog(
            title = "Clear App Data",
            message = "All application data on the device will be wiped remotely. Installed apps will remain but user data within them will be cleared.",
            icon = Icons.Outlined.CleaningServices,
            iconColor = Warning,
            confirmText = "Clear Data",
            confirmColor = Warning,
            isDark = isDark,
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                showClearDataDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✓ App data cleared")
                }
            }
        )
    }

    // Remote Wipe Dialog — TWO STEP
    if (showWipeDialog) {
        Dialog(
            onDismissRequest = {
                showWipeDialog = false
                wipeStep = 0
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) DarkBgSurface else BgSurface)
                    .border(1.dp, Danger.copy(alpha = 0.40f), RoundedCornerShape(24.dp))
                    .padding(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Danger icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Danger.copy(alpha = 0.10f))
                            .border(2.dp, Danger.copy(alpha = 0.40f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (wipeStep == 1) "Remote Wipe Device?" else "Final Confirmation",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Danger
                    )

                    Spacer(Modifier.height(12.dp))

                    if (wipeStep == 1) {
                        // Step 1
                        Text(
                            "⚠ This will permanently erase ALL data on this device. This action cannot be undone.",
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        // Warning info box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Danger.copy(alpha = 0.08f))
                                .border(1.dp, Danger.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = Danger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "All apps, data, settings, and accounts will be permanently removed. The device will be factory reset.",
                                    fontFamily = DMSans,
                                    fontSize = 12.sp,
                                    color = Danger.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurpleDim)
                                    .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        showWipeDialog = false
                                        wipeStep = 0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Cancel",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = PurpleCore
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Danger)
                                    .clickable { wipeStep = 2 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "I Understand",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Step 2 — type WIPE to confirm
                        Text(
                            "Type WIPE to confirm permanent device erasure.",
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        BasicTextField(
                            value = wipeConfirmText,
                            onValueChange = { wipeConfirmText = it.uppercase().take(4) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) DarkBgElevated else Color(0xFFFFE8E8))
                                .border(
                                    1.5.dp,
                                    if (wipeConfirmText == "WIPE") Danger else Danger.copy(alpha = 0.30f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Danger,
                                letterSpacing = 6.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (wipeConfirmText.isEmpty()) {
                                        Text(
                                            "W I P E",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 22.sp,
                                            color = Danger.copy(alpha = 0.25f),
                                            letterSpacing = 6.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    inner()
                                }
                            }
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurpleDim)
                                    .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        showWipeDialog = false
                                        wipeStep = 0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Cancel",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = PurpleCore
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (wipeConfirmText == "WIPE") Danger
                                        else Danger.copy(alpha = 0.3f)
                                    )
                                    .clickable(enabled = wipeConfirmText == "WIPE") {
                                        showWipeDialog = false
                                        wipeStep = 0
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("⚠ Device wipe initiated")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "WIPE DEVICE",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════
// INFO TAB
// ══════════════════════════════════════

@Composable
private fun InfoTab(device: Device, deviceResponse: DeviceResponse?, isDark: Boolean, textColor: Color) {
    // Device Summary
    DevoraCard(accentColor = PurpleCore, isDark = isDark) {
        SectionHeader(title = "DEVICE SUMMARY", isDark = isDark)

        Spacer(Modifier.height(4.dp))

        val infoItems = buildList {
            add("Device Model" to (deviceResponse?.deviceModel ?: "—"))
            add("Manufacturer" to (deviceResponse?.manufacturer ?: "—"))
            add("Android OS" to (deviceResponse?.osVersion?.let { "Android $it" } ?: "—"))
            add("SDK Version" to (deviceResponse?.sdkVersion ?: "—"))
            add("Serial Number" to (deviceResponse?.serialNumber?.ifBlank { "Restricted" } ?: "Restricted"))
            add("Device UUID" to device.deviceId)
        }

        infoItems.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontFamily = DMSans, fontSize = 13.sp, color = TextMuted)
                Text(
                    value,
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    color = when {
                        value.equals("ACTIVE", ignoreCase = true) -> Success
                        value.equals("ONLINE", ignoreCase = true) -> Success
                        value.equals("FLAGGED", ignoreCase = true) -> Danger
                        value.equals("OFFLINE", ignoreCase = true) -> TextMuted
                        else -> textColor
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            if (index < infoItems.size - 1) {
                HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Enrollment Details
    DevoraCard(accentColor = PurpleCore, isDark = isDark) {
        SectionHeader(title = "ENROLLMENT DETAILS", isDark = isDark)

        Spacer(Modifier.height(4.dp))

        val enrollItems = buildList {
            add("Employee" to (deviceResponse?.employeeName ?: "—"))
            add("Employee ID" to (deviceResponse?.employeeId ?: "—"))
            add("Enrollment" to (deviceResponse?.enrollmentMethod ?: "—"))
            add("Enrolled At" to (deviceResponse?.enrolledAt?.take(10) ?: "—"))
            add("Status" to (device.status))
            add("Record ID" to (device.api))
        }

        enrollItems.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontFamily = DMSans, fontSize = 13.sp, color = TextMuted)
                Text(
                    value,
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    color = when {
                        value.equals("ACTIVE", ignoreCase = true) -> Success
                        value.equals("ONLINE", ignoreCase = true) -> Success
                        value.equals("OFFLINE", ignoreCase = true) -> TextMuted
                        else -> textColor
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            if (index < enrollItems.size - 1) {
                HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Security Status
    DevoraCard(accentColor = Success, isDark = isDark) {
        SectionHeader(title = "SECURITY STATUS", isDark = isDark)

        Spacer(Modifier.height(4.dp))

        val securityItems = listOf(
            Triple("Screen Lock", "Enforced", Success),
            Triple("Encryption", "Enabled", Success),
            Triple("Root Detection", "Clear", Success),
            Triple("Play Protect", "Verified", Success),
            Triple("Last Security Scan", "2 mins ago", PurpleCore)
        )

        securityItems.forEachIndexed { index, (label, value, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontFamily = DMSans, fontSize = 13.sp, color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        value,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                    if (color == Success) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            if (index < securityItems.size - 1) {
                HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }
    }
}

// ══════════════════════════════════════
// APPS TAB
// ══════════════════════════════════════

@Composable
private fun AppsTab(deviceId: String, isDark: Boolean, textColor: Color) {
    var apps by remember { mutableStateOf<List<AppInventoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedApp by remember { mutableStateOf<AppInventoryItem?>(null) }

    val surfaceBg = if (isDark) DarkBgSurface else BgSurface

    LaunchedEffect(deviceId) {
        isLoading = true
        try {
            val response = RetrofitClient.api.getAppInventory(deviceId)
            if (response.isSuccessful) {
                apps = response.body() ?: emptyList()
                errorMsg = null
            } else {
                errorMsg = "Failed to load apps (${response.code()})"
            }
        } catch (e: Exception) {
            errorMsg = "Failed to load apps. Check connection."
            Log.e("AppsTab", "Error fetching apps: ${e.message}")
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PurpleCore, modifier = Modifier.size(32.dp))
        }
        return
    }

    if (errorMsg != null) {
        DevoraCard(accentColor = PurpleCore, isDark = isDark) {
            Text(
                errorMsg!!,
                fontFamily = DMSans,
                fontSize = 13.sp,
                color = Danger,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            )
        }
        return
    }

    val userApps = apps.filter { it.isSystemApp != true }
    val systemApps = apps.filter { it.isSystemApp == true }

    val filteredApps = apps.filter { app ->
        val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "USER" -> app.isSystemApp != true
            "SYSTEM" -> app.isSystemApp == true
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column {
        // Summary header
        SectionHeader(title = "INSTALLED APPLICATIONS", isDark = isDark)
        Spacer(Modifier.height(4.dp))
        Text(
            "${apps.size} apps · ${userApps.size} user · ${systemApps.size} system",
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            color = TextMuted
        )

        Spacer(Modifier.height(12.dp))

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(surfaceBg, RoundedCornerShape(12.dp))
                .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = PurpleCore,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search apps...",
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = textColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf(
                "ALL" to apps.size,
                "USER" to userApps.size,
                "SYSTEM" to systemApps.size
            )
            filters.forEach { (label, count) ->
                val isSelected = selectedFilter == label
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) PurpleCore.copy(alpha = 0.12f) else surfaceBg,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) PurpleCore.copy(alpha = 0.40f) else PurpleCore.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilter = label }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$label ($count)",
                        fontFamily = if (isSelected) PlusJakartaSans else DMSans,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isSelected) PurpleCore else TextMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // App list
        if (filteredApps.isEmpty()) {
            Text(
                if (searchQuery.isNotEmpty()) "No apps match your search."
                else "No apps found on this device.",
                fontFamily = DMSans,
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        filteredApps.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { selectedApp = app },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurpleDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = PurpleCore,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            app.appName,
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = textColor
                        )
                        Text(
                            app.packageName,
                            fontFamily = JetBrainsMono,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                        if (!app.versionName.isNullOrBlank()) {
                            Text(
                                "v${app.versionName}",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
                val statusLabel = if (app.isSystemApp == true) "System" else "User"
                val statusColor = if (app.isSystemApp == true) Warning else Success
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(statusColor.copy(alpha = 0.10f))
                        .border(
                            1.dp,
                            statusColor.copy(alpha = 0.25f),
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusLabel,
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (index < filteredApps.size - 1) {
                HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }

        // App details dialog
        if (selectedApp != null) {
            Dialog(onDismissRequest = { selectedApp = null }, properties = DialogProperties()) {
                DevoraCard(accentColor = PurpleCore, isDark = isDark) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = selectedApp!!.appName,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Package: ${selectedApp!!.packageName}",
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        if (!selectedApp!!.versionName.isNullOrBlank()) {
                            Text(
                                text = "Version: v${selectedApp!!.versionName}",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        if (selectedApp!!.versionCode != null) {
                            Text(
                                text = "Version code: ${selectedApp!!.versionCode}",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        if (!selectedApp!!.installSource.isNullOrBlank()) {
                            Text(
                                text = "Install source: ${selectedApp!!.installSource}",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "Close",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = PurpleCore,
                                modifier = Modifier.clickable { selectedApp = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════
// ACTIVITY TAB
// ══════════════════════════════════════

@Composable
private fun ActivityTab(isDark: Boolean, textColor: Color) {
    val activities = listOf(
        Triple("Device enrolled via QR code", "Today, 10:23 AM", Success),
        Triple("Enterprise policies applied", "Today, 10:24 AM", PurpleCore),
        Triple("Screen lock enforced", "Today, 10:24 AM", PurpleCore),
        Triple("App inventory scanned", "Today, 10:25 AM", Success),
        Triple("WhatsApp blocked by policy", "Today, 10:25 AM", Danger),
        Triple("Instagram blocked by policy", "Today, 10:25 AM", Danger),
        Triple("Device synced with server", "Today, 10:30 AM", Success),
        Triple("Security scan passed", "Today, 10:35 AM", Success),
        Triple("Location reported", "Today, 10:40 AM", PurpleCore),
        Triple("Battery level: 85%", "Today, 11:00 AM", Success)
    )

    DevoraCard(accentColor = PurpleCore, isDark = isDark) {
        SectionHeader(title = "DEVICE ACTIVITY LOG", isDark = isDark)

        activities.forEachIndexed { index, (event, time, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    event,
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    time,
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }
            if (index < activities.size - 1) {
                HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
            }
        }
    }
}

// ══════════════════════════════════════
// ACTIONS TAB
// ══════════════════════════════════════

@Composable
private fun ActionsTab(
    isDark: Boolean,
    textColor: Color,
    isSyncing: Boolean,
    onLock: () -> Unit,
    onPasswordReset: () -> Unit,
    onClearData: () -> Unit,
    onSync: () -> Unit,
    onWipe: () -> Unit
) {
    DevoraCard(accentColor = PurpleCore, isDark = isDark) {
        SectionHeader(title = "REMOTE ACTIONS", isDark = isDark)

        ActionRow(
            title = "Lock Device Now",
            subtitle = "Immediately lock device screen",
            icon = Icons.Outlined.Lock,
            color = PurpleCore,
            textColor = textColor,
            onClick = onLock
        )

        ActionRow(
            title = "Force Password Reset",
            subtitle = "Require employee to set new password",
            icon = Icons.Outlined.Password,
            color = Warning,
            textColor = textColor,
            onClick = onPasswordReset
        )

        ActionRow(
            title = "Clear App Data",
            subtitle = "Wipe all app data remotely",
            icon = Icons.Outlined.CleaningServices,
            color = Warning,
            textColor = textColor,
            onClick = onClearData
        )

        // Sync row with loading state
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isSyncing) { onSync() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PurpleCore.copy(alpha = 0.10f))
                        .border(1.dp, PurpleCore.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = PurpleCore,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Sync,
                            contentDescription = null,
                            tint = PurpleCore,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        if (isSyncing) "Syncing..." else "Sync Device Now",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Text(
                        "Force immediate data synchronization",
                        fontFamily = DMSans,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
            if (!isSyncing) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = PurpleCore,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)

        Spacer(Modifier.height(16.dp))

        // ── DANGER ZONE ──
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = Danger.copy(alpha = 0.20f),
                thickness = 1.dp,
                modifier = Modifier.align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(if (isDark) DarkBgSurface else BgSurface)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    "DANGER ZONE",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = Danger,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        ActionRow(
            title = "Remote Wipe Device",
            subtitle = "⚠ Permanently erase all device data",
            icon = Icons.Outlined.DeleteForever,
            color = Danger,
            textColor = textColor,
            onClick = onWipe
        )
    }
}

// ══════════════════════════════════════
// ACTION ROW
// ══════════════════════════════════════

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.10f))
                    .border(1.dp, color.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    subtitle,
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
    HorizontalDivider(color = PurpleCore.copy(alpha = 0.08f), thickness = 1.dp)
}

// ══════════════════════════════════════
// CONFIRM ACTION DIALOG (reusable)
// ══════════════════════════════════════

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconColor: Color,
    confirmText: String,
    confirmColor: Color,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDark) DarkBgSurface else BgSurface)
                .border(1.dp, PurpleBorder, RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.10f))
                        .border(1.dp, iconColor.copy(alpha = 0.30f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isDark) DarkTextPrimary else TextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    message,
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleDim)
                            .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = PurpleCore
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(confirmColor, confirmColor.copy(alpha = 0.85f))
                                )
                            )
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            confirmText,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
