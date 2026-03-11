package com.devora.devicemanager.ui.screens.dashboard

import android.util.Log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devora.devicemanager.ui.components.DevoraBottomNav
import com.devora.devicemanager.ui.components.DevoraCard
import com.devora.devicemanager.ui.components.SectionHeader
import com.devora.devicemanager.network.DashboardStats
import com.devora.devicemanager.network.DeviceActivityResponse
import com.devora.devicemanager.network.MdmAlertResponse
import com.devora.devicemanager.network.MarkAlertsReadRequest
import com.devora.devicemanager.network.RetrofitClient
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBright
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDeep
import com.devora.devicemanager.ui.theme.PurpleDim
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.Warning as WarningColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

// ══════════════════════════════════════
// STAT DATA CLASS
// ══════════════════════════════════════

private data class Stat(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

// ══════════════════════════════════════
// ACTIVITY DATA
// ══════════════════════════════════════

private data class Activity(
    val description: String,
    val device: String,
    val time: String,
    val color: Color
)

private fun formatTimeAgo(isoDateTime: String?): String {
    if (isoDateTime.isNullOrEmpty()) return "just now"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDateTime) ?: return "just now"
        val diffSec = (System.currentTimeMillis() - date.time) / 1000
        when {
            diffSec < 60 -> "just now"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            diffSec < 86400 -> "${diffSec / 3600}h ago"
            else -> "${diffSec / 86400}d ago"
        }
    } catch (e: Exception) {
        "just now"
    }
}

// ══════════════════════════════════════
// DASHBOARD SCREEN
// ══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val bgColor = if (isDark) DarkBgBase else BgBase
    val textColor = if (isDark) DarkTextPrimary else TextPrimary
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    }
    var dashboardStats by remember { mutableStateOf<DashboardStats?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getDashboardStats()
            if (response.isSuccessful) {
                dashboardStats = response.body()
            } else {
                Log.e("DashboardScreen", "Stats fetch failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("DashboardScreen", "Failed to fetch dashboard stats", e)
        }
    }

    // Real activities from device_activities table
    var recentActivities by remember { mutableStateOf<List<DeviceActivityResponse>>(emptyList()) }

    LaunchedEffect("activities") {
        while (true) {
            try {
                val response = RetrofitClient.api.getActivities(limit = 10)
                if (response.isSuccessful) {
                    recentActivities = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("DashboardScreen", "Failed to fetch activities", e)
            }
            delay(30_000L) // refresh every 30s
        }
    }

    // Unread alert count for bell badge
    var unreadCount by remember { mutableStateOf(0) }
    var showAlerts by remember { mutableStateOf(false) }
    var unreadAlerts by remember { mutableStateOf<List<MdmAlertResponse>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect("alertCount") {
        while (true) {
            try {
                val resp = RetrofitClient.api.getUnreadAlertCount()
                if (resp.isSuccessful) {
                    unreadCount = resp.body()?.count ?: 0
                }
            } catch (_: Exception) { }
            delay(60_000L) // poll every 60s
        }
    }

    val totalDevices = dashboardStats?.totalDevices ?: 0
    val activeDevices = dashboardStats?.activeDevices ?: 0
    val inactiveDevices = (totalDevices - activeDevices).coerceAtLeast(0)
    val violations = dashboardStats?.violations ?: inactiveDevices
    val onlineRatio = if (totalDevices > 0) activeDevices.toFloat() / totalDevices else 0f
    val onlinePercent = (onlineRatio * 100).toInt()

    val stats = listOf(
        Stat(totalDevices.toString(), "TOTAL DEVICES", Icons.Filled.Devices, PurpleCore),
        Stat(activeDevices.toString(), "ACTIVE NOW", Icons.Filled.CheckCircle, Success),
        Stat(violations.toString(), "VIOLATIONS", Icons.Filled.Warning, Danger),
        Stat(inactiveDevices.toString(), "PENDING", Icons.Filled.Schedule, WarningColor)
    )

    Scaffold(
        bottomBar = {
            DevoraBottomNav(
                currentRoute = "dashboard",
                onNavigate = onNavigate,
                isDark = isDark
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // TOP BAR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DEVORA",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PurpleCore
                        )
                        Text(
                            text = "Admin Panel",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onThemeToggle) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = PurpleCore
                            )
                        }
                        Box {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    try {
                                        val resp = RetrofitClient.api.getUnreadAlerts()
                                        if (resp.isSuccessful) {
                                            unreadAlerts = resp.body() ?: emptyList()
                                        }
                                    } catch (_: Exception) { }
                                    showAlerts = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PurpleCore
                                )
                            }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Danger, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else "$unreadCount",
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // GREETING CARD
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DevoraCard(showTopAccent = true, isDark = isDark) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Good Morning, Admin",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentDate,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(PurpleCore, PurpleDeep)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // STATS ROW
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stats) { stat ->
                        Box(modifier = Modifier.width(150.dp).height(100.dp)) {
                            DevoraCard(
                                accentColor = stat.color,
                                isDark = isDark
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Icon top-right
                                    Icon(
                                        imageVector = stat.icon,
                                        contentDescription = null,
                                        tint = stat.color.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .align(Alignment.TopEnd)
                                    )
                                    // Value and label bottom-left
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    ) {
                                        Text(
                                            text = stat.value,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 30.sp,
                                            color = stat.color
                                        )
                                        Text(
                                            text = stat.label,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // DEVICE HEALTH
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DevoraCard(
                        accentColor = PurpleCore,
                        isDark = isDark
                    ) {
                        Column {
                            SectionHeader(
                                title = "DEVICE HEALTH",
                                actionText = "$totalDevices Total",
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$activeDevices ONLINE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Success
                                )
                                Text(
                                    text = "$inactiveDevices OFFLINE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Health bar
                            val barBg = if (isDark) com.devora.devicemanager.ui.theme.DarkBgElevated else BgElevated
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = barBg,
                                            cornerRadius = CornerRadius(6.dp.toPx())
                                        )
                                        drawRoundRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(PurpleCore, PurpleBright)
                                            ),
                                            size = Size(size.width * onlineRatio, size.height),
                                            cornerRadius = CornerRadius(6.dp.toPx())
                                        )
                                    }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$onlinePercent%",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${100 - onlinePercent}%",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // RECENT ACTIVITY HEADER
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = "RECENT ACTIVITY",
                        actionText = "See All",
                        onActionClick = { },
                        isDark = isDark
                    )
                }
            }

            // ACTIVITY ROWS
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DevoraCard(isDark = isDark) {
                        val activities = recentActivities.map { a ->
                            Activity(
                                description = a.description ?: "Unknown activity",
                                device = if (!a.deviceId.isNullOrBlank()) "Device ···${a.deviceId!!.takeLast(6)}" else "",
                                time = formatTimeAgo(a.createdAt),
                                color = when (a.severity) {
                                    "CRITICAL" -> Danger
                                    "WARNING" -> WarningColor
                                    else -> Success
                                }
                            )
                        }
                        if (activities.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = TextMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No recent activity",
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        } else {
                            Column {
                                activities.forEachIndexed { index, activity ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(activity.color, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = activity.description,
                                                fontFamily = DMSans,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = textColor
                                            )
                                            Text(
                                                text = activity.device,
                                                fontFamily = DMSans,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = activity.time,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    if (index < activities.size - 1) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = if (isDark) {
                                                PurpleCore.copy(alpha = 0.10f)
                                            } else {
                                                BgElevated
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // ══════════════════════════════════════
    // ALERTS BOTTOM SHEET
    // ══════════════════════════════════════
    if (showAlerts) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAlerts = false },
            sheetState = sheetState,
            containerColor = if (isDark) DarkBgBase else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 200.dp, max = 500.dp)
            ) {
                Text(
                    "Unread Alerts",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (unreadAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No unread alerts",
                            fontFamily = DMSans,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn {
                        items(unreadAlerts) { alert ->
                            val alertColor = when (alert.severity) {
                                "CRITICAL" -> Danger
                                "WARNING" -> WarningColor
                                else -> PurpleCore
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(alertColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.message ?: "Alert",
                                        fontFamily = DMSans,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = formatTimeAgo(alert.createdAt),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                // Dismiss button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(alertColor.copy(alpha = 0.10f))
                                        .clickable {
                                            coroutineScope.launch {
                                                try {
                                                    RetrofitClient.api.markAlertsRead(
                                                        MarkAlertsReadRequest(listOf(alert.id))
                                                    )
                                                    unreadAlerts = unreadAlerts.filter { it.id != alert.id }
                                                    unreadCount = (unreadCount - 1).coerceAtLeast(0)
                                                } catch (_: Exception) { }
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Dismiss",
                                        fontFamily = DMSans,
                                        fontSize = 11.sp,
                                        color = alertColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = if (isDark) PurpleCore.copy(alpha = 0.10f) else BgElevated
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}