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
import com.devora.devicemanager.network.AdminNotification
import com.devora.devicemanager.ui.theme.Warning as WarningColor
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    if (isoDateTime == null) return "just now"
    return try {
        val truncated = isoDateTime.substringBefore('.')
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(truncated) ?: return "just now"
        val diff = (System.currentTimeMillis() - date.time) / 1000
        when {
            diff < 60    -> "${diff}s ago"
            diff < 3600  -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else         -> "${diff / 86400}d ago"
        }
    } catch (e: Exception) { "just now" }
}

// ══════════════════════════════════════
// DASHBOARD SCREEN
// ══════════════════════════════════════

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
    var notifications by remember { mutableStateOf<List<AdminNotification>>(emptyList()) }
    var unreadCount by remember { mutableStateOf(0) }

    // Poll backend every 10 seconds — new app installs show up within seconds
    LaunchedEffect("notifications") {
        while (true) {
            try {
                val resp = RetrofitClient.api.getNotifications()
                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList()
                    notifications = list.take(10)
                    unreadCount = list.count { !it.read }
                }
            } catch (e: Exception) {
                Log.e("DashboardScreen", "Failed to fetch notifications", e)
            }
            delay(10_000L)
        }
    }

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
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PurpleCore
                                )
                            }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(if (unreadCount > 9) 16.dp else 14.dp)
                                        .background(Danger, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 7.sp,
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
                        val activities = notifications.map { n ->
                            Activity(
                                description = n.message ?: n.title,
                                device = n.title,
                                time = formatTimeAgo(n.createdAt),
                                color = when {
                                    n.type.endsWith("INSTALLED") -> Success
                                    n.type.endsWith("UPDATED")   -> WarningColor
                                    n.type.endsWith("REMOVED")   -> Danger
                                    else -> PurpleCore
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
}
