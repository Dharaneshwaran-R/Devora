package com.devora.devicemanager.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBright
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDeep
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.Warning as WarningColor
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

private val stats = listOf(
    Stat("24", "TOTAL DEVICES", Icons.Filled.Devices, PurpleCore),
    Stat("18", "ACTIVE NOW", Icons.Filled.CheckCircle, Success),
    Stat("0", "VIOLATIONS", Icons.Filled.Warning, Danger),
    Stat("5", "PENDING", Icons.Filled.Schedule, WarningColor)
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

private val recentActivities = listOf(
    Activity("Policy update pushed", "Galaxy S24 Ultra", "2m ago", Success),
    Activity("New device enrollment", "Pixel Tablet", "2m ago", PurpleCore),
    Activity("Security violation", "Manager-Laptop", "2m ago", Danger),
    Activity("Remote wipe completed", "Stolen-Phone-01", "2m ago", WarningColor),
    Activity("System sync finished", "Global Cloud", "2m ago", Success)
)

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
    val currentDate = remember@Composable {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    }

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
                            // Red dot badge
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Danger, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                            )
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
                                actionText = "24 Total",
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "18 ONLINE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Success
                                )
                                Text(
                                    text = "6 OFFLINE",
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
                                            size = Size(size.width * 0.75f, size.height),
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
                                    text = "75%",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "25%",
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
                        Column {
                            recentActivities.forEachIndexed { index, activity ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(activity.color, CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Description + device
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

                                    // Time
                                    Text(
                                        text = activity.time,
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                if (index < recentActivities.size - 1) {
                                    Divider(
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
