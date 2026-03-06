package com.devora.devicemanager.ui.screens.employeedashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devora.devicemanager.ui.components.DevoraCard
import com.devora.devicemanager.ui.components.SectionHeader
import com.devora.devicemanager.ui.components.StatusBadge
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgSurface
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.DarkBgSurface
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDim
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import com.devora.devicemanager.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeDashboardScreen(
    onSignOut: () -> Unit,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    var selectedNavItem by remember { mutableIntStateOf(0) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDark) DarkBgBase else BgBase
    val textColor = if (isDark) DarkTextPrimary else TextPrimary
    val surfaceBg = if (isDark) DarkBgSurface else BgSurface

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            EmployeeBottomNav(
                selectedIndex = selectedNavItem,
                onItemSelected = { selectedNavItem = it },
                isDark = isDark
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ══════════════════════════════════════
            // TOP BAR
            // ══════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "DEVORA",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = PurpleCore,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "My Device",
                        fontFamily = DMSans,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = PurpleCore
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = PurpleCore
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // WELCOME CARD
            // ══════════════════════════════════════
            DevoraCard(showTopAccent = true, accentColor = Success, isDark = isDark) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hi, Employee!",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val pulse by rememberInfiniteTransition(label = "pulse")
                                .animateFloat(
                                    initialValue = 0.6f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        tween(1000),
                                        RepeatMode.Reverse
                                    ),
                                    label = "pulseAlpha"
                                )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Success.copy(alpha = pulse))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Device is being monitored",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Enrolled: ${
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            }",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Success, Color(0xFF059669)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "E",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // MY DEVICE STATUS CARD
            // ══════════════════════════════════════
            DevoraCard(accentColor = Success, isDark = isDark) {
                SectionHeader(title = "MY DEVICE STATUS", isDark = isDark)

                // Device name row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            Build.MODEL,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Text(
                            Build.MANUFACTURER,
                            fontFamily = DMSans,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    StatusBadge(status = "ONLINE")
                }

                Spacer(Modifier.height(12.dp))

                // 2x2 status grid — Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cell 1: Enrollment status
                    DevoraCard(modifier = Modifier.weight(1f), isDark = isDark) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Success)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "ENROLLED",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    color = Success
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Active",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }

                    // Cell 2: Policy status
                    DevoraCard(modifier = Modifier.weight(1f), isDark = isDark) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PurpleCore)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "POLICY",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    color = PurpleCore
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Active",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 2x2 status grid — Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cell 3: Last sync
                    DevoraCard(modifier = Modifier.weight(1f), isDark = isDark) {
                        Column {
                            Text(
                                "LAST SYNC",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "2 mins ago",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }

                    // Cell 4: Device health
                    DevoraCard(modifier = Modifier.weight(1f), isDark = isDark) {
                        Column {
                            Text(
                                "HEALTH",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Good",
                                    fontFamily = DMSans,
                                    fontSize = 12.sp,
                                    color = Success
                                )
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
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // DEVICE INFORMATION CARD
            // ══════════════════════════════════════
            DevoraCard(accentColor = PurpleCore, isDark = isDark) {
                SectionHeader(title = "DEVICE INFORMATION", isDark = isDark)

                val infoItems = listOf(
                    Triple("Model", Build.MODEL, textColor),
                    Triple(
                        "Manufacturer",
                        Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                        textColor
                    ),
                    Triple("Android", Build.VERSION.RELEASE, textColor),
                    Triple("SDK Level", Build.VERSION.SDK_INT.toString(), textColor),
                    Triple("Status", "Enrolled ✓", Success),
                    Triple("Policy", "Enterprise Standard", PurpleCore),
                    Triple("Server", "Connected", Success)
                )

                infoItems.forEachIndexed { index, (label, value, valueColor) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            fontFamily = DMSans,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            value,
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            color = valueColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (index < infoItems.size - 1) {
                        HorizontalDivider(
                            color = PurpleCore.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // ACTIVE POLICIES CARD
            // ══════════════════════════════════════
            DevoraCard(accentColor = PurpleCore, isDark = isDark) {
                SectionHeader(title = "ACTIVE POLICIES ON THIS DEVICE", isDark = isDark)

                data class PolicyStatus(
                    val icon: ImageVector,
                    val name: String,
                    val status: String,
                    val statusColor: Color
                )

                val policies = listOf(
                    PolicyStatus(Icons.Outlined.Lock, "Screen Lock", "Active", Success),
                    PolicyStatus(
                        Icons.Outlined.Shield,
                        "Factory Reset Protection",
                        "Active",
                        Success
                    ),
                    PolicyStatus(
                        Icons.Outlined.Block,
                        "App Install Restriction",
                        "Restricted",
                        Warning
                    ),
                    PolicyStatus(Icons.Outlined.CameraAlt, "Camera", "Enabled", Success),
                    PolicyStatus(Icons.Outlined.Wifi, "Network Monitoring", "Active", Success)
                )

                policies.forEachIndexed { index, policy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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
                                    policy.icon,
                                    contentDescription = null,
                                    tint = PurpleCore,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                policy.name,
                                fontFamily = DMSans,
                                fontSize = 13.sp,
                                color = textColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(policy.statusColor.copy(alpha = 0.10f))
                                .border(
                                    1.dp,
                                    policy.statusColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                policy.status,
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = policy.statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (index < policies.size - 1) {
                        HorizontalDivider(
                            color = PurpleCore.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // RECENT ACTIVITY
            // ══════════════════════════════════════
            SectionHeader(title = "MY RECENT ACTIVITY", isDark = isDark)

            Spacer(Modifier.height(8.dp))

            val myActivities = listOf(
                Triple("Device enrolled successfully", "Just now", Success),
                Triple("Enterprise policies applied", "1 min ago", PurpleCore),
                Triple("Server connection established", "2 min ago", Success),
                Triple("Device Owner mode activated", "3 min ago", PurpleCore),
                Triple("Security scan completed", "5 min ago", Success)
            )

            DevoraCard(isDark = isDark) {
                myActivities.forEachIndexed { index, (event, time, color) ->
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
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    if (index < myActivities.size - 1) {
                        HorizontalDivider(
                            color = PurpleCore.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // SUPPORT CARD
            // ══════════════════════════════════════
            DevoraCard(isDark = isDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:admin@enterprise.com")
                            }
                            context.startActivity(intent)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PurpleDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.SupportAgent,
                                contentDescription = null,
                                tint = PurpleCore,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "IT Support",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = textColor
                            )
                            Text(
                                "Contact your administrator",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = PurpleCore,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══════════════════════════════════════
            // SIGN OUT
            // ══════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Danger.copy(alpha = 0.06f))
                    .border(1.dp, Danger.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .clickable { showSignOutDialog = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Sign Out",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Danger
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Your IT team monitors this device for security",
                fontFamily = DMSans,
                fontSize = 11.sp,
                color = TextMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // ══════════════════════════════════════
    // SIGN OUT DIALOG
    // ══════════════════════════════════════
    if (showSignOutDialog) {
        Dialog(
            onDismissRequest = { showSignOutDialog = false },
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
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Danger.copy(alpha = 0.10f))
                            .border(1.dp, Danger.copy(alpha = 0.30f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Sign Out?",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (isDark) DarkTextPrimary else TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Your device will remain enrolled and managed. You can sign back in anytime.",
                        fontFamily = DMSans,
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PurpleDim)
                                .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                .clickable { showSignOutDialog = false },
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

                        // Sign Out
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Danger, Color(0xFFE11D48))
                                    )
                                )
                                .clickable {
                                    showSignOutDialog = false
                                    onSignOut()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Sign Out",
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
    }
}

// ══════════════════════════════════════
// EMPLOYEE BOTTOM NAV (3 items)
// ══════════════════════════════════════

private data class EmpNavItem(
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
)

private val empNavItems = listOf(
    EmpNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
    EmpNavItem("My Device", Icons.Outlined.PhoneAndroid, Icons.Filled.PhoneAndroid),
    EmpNavItem("Support", Icons.Outlined.SupportAgent, Icons.Filled.SupportAgent)
)

@Composable
private fun EmployeeBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    isDark: Boolean
) {
    val surfaceBg = if (isDark) DarkBgSurface else BgSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceBg)
    ) {
        // Top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PurpleCore.copy(alpha = 0.12f))
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            empNavItems.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(index) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 40.dp else 36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) PurpleCore.copy(alpha = 0.12f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) PurpleCore else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    // Selected dot
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(PurpleCore)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        item.label,
                        fontFamily = if (isSelected) PlusJakartaSans else DMSans,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 10.sp,
                        color = if (isSelected) PurpleCore else TextMuted
                    )
                }
            }
        }
    }
}
