package com.devora.devicemanager.ui.screens.enrollment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devora.devicemanager.ui.components.DevoraCard
import com.devora.devicemanager.ui.components.SectionHeader
import com.devora.devicemanager.ui.components.StatusBadge
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.DarkBgElevated
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleBright
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDim
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import com.devora.devicemanager.ui.theme.Warning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════
// DATA CLASS
// ══════════════════════════════════════

data class EnrollmentSession(
    val id: String,
    val deviceLabel: String,
    val assignedEmployee: String,
    val department: String,
    val deviceType: String,
    val token: String,
    val validityHours: Int,
    val createdAt: Long,
    val expiresAt: Long,
    val status: String
)

// ══════════════════════════════════════
// SCREEN
// ══════════════════════════════════════

@Composable
fun AdminGenerateEnrollmentScreen(
    onBack: () -> Unit,
    isDark: Boolean
) {
    var screenState by remember { mutableStateOf("FORM") }
    var deviceLabel by remember { mutableStateOf("") }
    var assignedEmployee by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("") }
    var selectedDeviceType by remember { mutableStateOf("") }
    var selectedValidity by remember { mutableStateOf("24h") }
    var enrollType by remember { mutableStateOf("QR") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedToken by remember { mutableStateOf("") }
    var showDeptDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }
    var revokeTargetId by remember { mutableStateOf("") }
    var showPayload by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val textColor = if (isDark) Color(0xFFF2EFFF) else TextPrimary
    val inputBg = if (isDark) DarkBgElevated else BgElevated

    val activeEnrollments = remember {
        mutableStateListOf(
            EnrollmentSession(
                id = "1",
                deviceLabel = "Marketing-Phone-01",
                assignedEmployee = "Ravi Kumar",
                department = "Marketing",
                deviceType = "Smartphone",
                token = "DEV-A3X9-K2P7-MN4Q",
                validityHours = 24,
                createdAt = System.currentTimeMillis() - 3600000,
                expiresAt = System.currentTimeMillis() + 82800000,
                status = "PENDING"
            ),
            EnrollmentSession(
                id = "2",
                deviceLabel = "Finance-Tab-03",
                assignedEmployee = "Priya S",
                department = "Finance",
                deviceType = "Tablet",
                token = "DEV-B7KL-Q4RT-XP2M",
                validityHours = 48,
                createdAt = System.currentTimeMillis() - 7200000,
                expiresAt = System.currentTimeMillis() + 165600000,
                status = "PENDING"
            )
        )
    }

    fun generateToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "DEV-" + (1..3).joinToString("-") {
            (1..4).map { chars.random() }.joinToString("")
        }
    }

    Scaffold(
        containerColor = if (isDark) DarkBgBase else BgBase,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ══════ TOP BAR ══════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PurpleCore,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (screenState == "FORM") "New Enrollment" else "Enrollment Created",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Spacer(Modifier.weight(1f))
                    if (screenState == "GENERATED") {
                        IconButton(onClick = {
                            screenState = "FORM"
                            deviceLabel = ""
                            assignedEmployee = ""
                            selectedDepartment = ""
                            selectedDeviceType = ""
                            generatedToken = ""
                            showPayload = false
                        }) {
                            Icon(Icons.Outlined.Add, tint = PurpleCore, contentDescription = "New", modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Spacer(Modifier.width(48.dp))
                    }
                }
            }

            // ══════════════════════════════════════
            // FORM STATE
            // ══════════════════════════════════════

            if (screenState == "FORM") {

                // ── ENROLLMENT CONFIGURATION CARD ──
                item {
                    DevoraCard(isDark = isDark, showTopAccent = true, accentColor = PurpleCore) {
                        Column {
                            SectionHeader(title = "ENROLLMENT CONFIGURATION", isDark = isDark)

                            Spacer(Modifier.height(8.dp))

                            // Device Label
                            Text("Device Label / Name", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(6.dp))
                            BasicTextField(
                                value = deviceLabel,
                                onValueChange = { deviceLabel = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(inputBg)
                                    .padding(14.dp),
                                textStyle = TextStyle(fontFamily = DMSans, fontSize = 14.sp, color = textColor),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (deviceLabel.isEmpty()) {
                                        Text("e.g. Marketing-Phone-01", fontFamily = DMSans, fontSize = 14.sp, color = TextMuted)
                                    }
                                    inner()
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            // Assigned Employee
                            Text("Assigned Employee", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(6.dp))
                            BasicTextField(
                                value = assignedEmployee,
                                onValueChange = { assignedEmployee = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(inputBg)
                                    .padding(14.dp),
                                textStyle = TextStyle(fontFamily = DMSans, fontSize = 14.sp, color = textColor),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (assignedEmployee.isEmpty()) {
                                        Text("Employee name or ID", fontFamily = DMSans, fontSize = 14.sp, color = TextMuted)
                                    }
                                    inner()
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            // Department dropdown
                            Text("Department", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(inputBg)
                                        .then(
                                            if (showDeptDropdown) Modifier.border(1.dp, PurpleCore, RoundedCornerShape(10.dp))
                                            else Modifier
                                        )
                                        .clickable { showDeptDropdown = true }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDepartment.ifEmpty { "Select Department" },
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = if (selectedDepartment.isEmpty()) TextMuted else textColor
                                        )
                                        Icon(
                                            imageVector = if (showDeptDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = PurpleCore,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showDeptDropdown,
                                    onDismissRequest = { showDeptDropdown = false },
                                    modifier = Modifier.background(if (isDark) Color(0xFF1E1E32) else Color.White)
                                ) {
                                    listOf("IT", "HR", "Finance", "Operations", "Sales", "Management").forEach { dept ->
                                        DropdownMenuItem(
                                            text = { Text(dept, fontFamily = DMSans, fontSize = 14.sp, color = textColor) },
                                            onClick = {
                                                selectedDepartment = dept
                                                showDeptDropdown = false
                                            },
                                            leadingIcon = {
                                                if (selectedDepartment == dept) {
                                                    Icon(Icons.Filled.Check, tint = PurpleCore, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Device Type dropdown
                            Text("Device Type", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(inputBg)
                                        .then(
                                            if (showTypeDropdown) Modifier.border(1.dp, PurpleCore, RoundedCornerShape(10.dp))
                                            else Modifier
                                        )
                                        .clickable { showTypeDropdown = true }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDeviceType.ifEmpty { "Select Device Type" },
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = if (selectedDeviceType.isEmpty()) TextMuted else textColor
                                        )
                                        Icon(
                                            imageVector = if (showTypeDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = PurpleCore,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showTypeDropdown,
                                    onDismissRequest = { showTypeDropdown = false },
                                    modifier = Modifier.background(if (isDark) Color(0xFF1E1E32) else Color.White)
                                ) {
                                    listOf("Smartphone", "Tablet", "Laptop").forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type, fontFamily = DMSans, fontSize = 14.sp, color = textColor) },
                                            onClick = {
                                                selectedDeviceType = type
                                                showTypeDropdown = false
                                            },
                                            leadingIcon = {
                                                if (selectedDeviceType == type) {
                                                    Icon(Icons.Filled.Check, tint = PurpleCore, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Token Validity
                            Text("Token Validity", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("24h", "48h", "7 days").forEach { validity ->
                                    val isSelected = selectedValidity == validity
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(if (isSelected) PurpleCore else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.Transparent else Color(0x407C3AED),
                                                RoundedCornerShape(100.dp)
                                            )
                                            .clickable { selectedValidity = validity }
                                            .padding(horizontal = 18.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            validity,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.White else PurpleCore
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Enrollment Type
                            Text("Enrollment Type", fontFamily = DMSans, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PurpleCore)
                            Spacer(Modifier.height(8.dp))
                            listOf(
                                "QR" to "QR Code (Recommended)",
                                "TOKEN" to "Token Only"
                            ).forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (enrollType == value) PurpleDim else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (enrollType == value) Color(0x407C3AED) else Color(0x1A7C3AED),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { enrollType = value }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = enrollType == value,
                                        onClick = { enrollType = value },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = PurpleCore,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        label,
                                        fontFamily = DMSans,
                                        fontSize = 14.sp,
                                        color = if (enrollType == value) PurpleCore else textColor,
                                        fontWeight = if (enrollType == value) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // ── GENERATE BUTTON ──
                item {
                    Button(
                        onClick = {
                            if (deviceLabel.isBlank() || assignedEmployee.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fill all required fields")
                                }
                                return@Button
                            }
                            coroutineScope.launch {
                                isGenerating = true
                                delay(1500)
                                generatedToken = generateToken()
                                isGenerating = false
                                screenState = "GENERATED"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleCore),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Generate Enrollment",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // ── ACTIVE ENROLLMENTS ──
                item {
                    SectionHeader(
                        title = "ACTIVE ENROLLMENT SESSIONS",
                        actionText = "${activeEnrollments.size} Active",
                        isDark = isDark
                    )
                }

                items(activeEnrollments.toList(), key = { it.id }) { session ->
                    ActiveEnrollmentCard(
                        session = session,
                        isDark = isDark,
                        textColor = textColor,
                        onRevoke = {
                            revokeTargetId = session.id
                            showRevokeDialog = true
                        }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            // ══════════════════════════════════════
            // GENERATED STATE
            // ══════════════════════════════════════

            if (screenState == "GENERATED") {

                // ── SUCCESS BANNER ──
                item {
                    DevoraCard(isDark = isDark, showTopAccent = true, accentColor = Success) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Success.copy(alpha = 0.12f))
                                    .border(1.dp, Success.copy(alpha = 0.30f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Enrollment Created!",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Success
                                )
                                Text(
                                    "Valid for $selectedValidity",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // ── QR CODE CARD ──
                item {
                    DevoraCard(isDark = isDark, accentColor = PurpleCore) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SectionHeader(title = "QR CODE FOR DEVICE SETUP", isDark = isDark)
                            Spacer(Modifier.height(16.dp))

                            // QR code canvas
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(2.dp, PurpleCore, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(Modifier.size(220.dp)) {
                                    val cellSize = size.width / 29f
                                    val gap = cellSize * 0.2f
                                    val margin = cellSize * 2

                                    fun drawDetectionSquare(offsetX: Float, offsetY: Float) {
                                        for (r in 0..6) {
                                            for (c in 0..6) {
                                                if (r == 0 || r == 6 || c == 0 || c == 6) {
                                                    drawRoundRect(
                                                        color = PurpleCore,
                                                        topLeft = Offset(offsetX + c * (cellSize + gap), offsetY + r * (cellSize + gap)),
                                                        size = Size(cellSize, cellSize),
                                                        cornerRadius = CornerRadius(2.dp.toPx())
                                                    )
                                                }
                                            }
                                        }
                                        for (r in 2..4) {
                                            for (c in 2..4) {
                                                drawRoundRect(
                                                    color = PurpleCore,
                                                    topLeft = Offset(offsetX + c * (cellSize + gap), offsetY + r * (cellSize + gap)),
                                                    size = Size(cellSize, cellSize),
                                                    cornerRadius = CornerRadius(2.dp.toPx())
                                                )
                                            }
                                        }
                                    }

                                    // Position detection patterns
                                    drawDetectionSquare(margin, margin)
                                    drawDetectionSquare(size.width - margin - 7 * (cellSize + gap), margin)
                                    drawDetectionSquare(margin, size.height - margin - 7 * (cellSize + gap))

                                    // Data cells
                                    val seed = generatedToken.hashCode()
                                    val random = java.util.Random(seed.toLong())
                                    for (row in 0..28) {
                                        for (col in 0..28) {
                                            val inTopLeft = row < 9 && col < 9
                                            val inTopRight = row < 9 && col > 19
                                            val inBottomLeft = row > 19 && col < 9
                                            if (inTopLeft || inTopRight || inBottomLeft) continue
                                            if (random.nextBoolean()) {
                                                drawRoundRect(
                                                    color = PurpleCore.copy(alpha = 0.85f),
                                                    topLeft = Offset(margin + col * (cellSize + gap), margin + row * (cellSize + gap)),
                                                    size = Size(cellSize, cellSize),
                                                    cornerRadius = CornerRadius(1.5.dp.toPx())
                                                )
                                            }
                                        }
                                    }

                                    // Timing patterns
                                    for (i in 8..20) {
                                        if (i % 2 == 0) {
                                            drawRoundRect(
                                                PurpleCore,
                                                Offset(margin + i * (cellSize + gap), margin + 6 * (cellSize + gap)),
                                                Size(cellSize, cellSize),
                                                CornerRadius(1.dp.toPx())
                                            )
                                            drawRoundRect(
                                                PurpleCore,
                                                Offset(margin + 6 * (cellSize + gap), margin + i * (cellSize + gap)),
                                                Size(cellSize, cellSize),
                                                CornerRadius(1.dp.toPx())
                                            )
                                        }
                                    }
                                }

                                // Center branding circle
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0x407C3AED), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "D",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = PurpleCore
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Scan during Android device setup wizard",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Download
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                                        .clickable { /* mock download */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Download, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Download", fontFamily = DMSans, fontSize = 13.sp, color = PurpleCore, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Share
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PurpleDim)
                                        .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "DEVORA Enrollment QR\nToken: $generatedToken\nDevice: $deviceLabel"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Share QR", fontFamily = DMSans, fontSize = 13.sp, color = PurpleCore, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── TOKEN CARD ──
                item {
                    DevoraCard(isDark = isDark, accentColor = Warning) {
                        Column {
                            SectionHeader(title = "ENROLLMENT TOKEN", isDark = isDark)

                            Text(
                                "Alternative to QR code — employee enters this manually",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Spacer(Modifier.height(12.dp))

                            // Token display
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF0C0C16) else Color(0xFFF5F3FF))
                                    .border(1.5.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = generatedToken,
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = PurpleCore,
                                    letterSpacing = 3.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Copy Token
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                                            clipboard.setPrimaryClip(ClipData.newPlainText("token", generatedToken))
                                            Toast.makeText(context, "Token copied!", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Copy Token", fontFamily = DMSans, fontSize = 13.sp, color = PurpleCore, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Share Token
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PurpleDim)
                                        .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "DEVORA Enrollment Token: $generatedToken")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Share Token", fontFamily = DMSans, fontSize = 13.sp, color = PurpleCore, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── QR PAYLOAD (ADVANCED) ──
                item {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF10101E) else Color(0xFFF5F3FF))
                                .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp))
                                .clickable { showPayload = !showPayload }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Code, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("QR Payload (Advanced)", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PurpleCore)
                                }
                                Icon(
                                    imageVector = if (showPayload) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = PurpleCore,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (showPayload) {
                            Spacer(Modifier.height(8.dp))

                            val jsonPayload = """
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":
    "com.devora.devicemanager/.receiver.DeviceAdminReceiver",

  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION":
    "https://yourserver.com/devora.apk",

  "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE": {
    "enrollment_token": "$generatedToken",
    "server_url": "https://yourserver.com/api",
    "device_label": "$deviceLabel"
  }
}""".trimIndent()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF08080F) else Color(0xFF1A0F33))
                                    .padding(16.dp)
                            ) {
                                SelectionContainer {
                                    Text(
                                        jsonPayload,
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                        color = PurpleBright,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, PurpleBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                                        clipboard.setPrimaryClip(ClipData.newPlainText("payload", jsonPayload))
                                        Toast.makeText(context, "JSON copied!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = PurpleCore, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy JSON", fontFamily = DMSans, fontSize = 12.sp, color = PurpleCore, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ── ACTIVE ENROLLMENTS ──
                item {
                    SectionHeader(
                        title = "ACTIVE ENROLLMENT SESSIONS",
                        actionText = "${activeEnrollments.size} Active",
                        isDark = isDark
                    )
                }

                items(activeEnrollments.toList(), key = { it.id }) { session ->
                    ActiveEnrollmentCard(
                        session = session,
                        isDark = isDark,
                        textColor = textColor,
                        onRevoke = {
                            revokeTargetId = session.id
                            showRevokeDialog = true
                        }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // ── REVOKE DIALOG ──
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            containerColor = if (isDark) Color(0xFF10101E) else Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Revoke Enrollment?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            },
            text = {
                Text(
                    "This token will be invalidated immediately.",
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeEnrollments.removeIf { it.id == revokeTargetId }
                        showRevokeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Revoke", fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancel", fontFamily = DMSans, fontSize = 14.sp, color = PurpleCore)
                }
            }
        )
    }
}

// ══════════════════════════════════════
// ACTIVE ENROLLMENT CARD
// ══════════════════════════════════════

@Composable
private fun ActiveEnrollmentCard(
    session: EnrollmentSession,
    isDark: Boolean,
    textColor: Color,
    onRevoke: () -> Unit
) {
    DevoraCard(isDark = isDark, accentColor = Warning, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Warning.copy(alpha = 0.12f))
                    .border(1.dp, Warning.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.deviceLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    session.assignedEmployee,
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(Modifier.height(4.dp))

                // Token badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PurpleDim)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(session.token, fontFamily = JetBrainsMono, fontSize = 10.sp, color = PurpleCore)
                }

                Spacer(Modifier.height(4.dp))

                // Countdown timer
                val timeLeft by produceState(
                    initialValue = "Calculating...",
                    key1 = session.expiresAt
                ) {
                    while (true) {
                        val remaining = session.expiresAt - System.currentTimeMillis()
                        if (remaining <= 0) {
                            value = "Expired"
                            break
                        }
                        val h = remaining / 3600000
                        val m = (remaining % 3600000) / 60000
                        val s = (remaining % 60000) / 1000
                        value = "Expires in ${h}h ${m}m ${s}s"
                        delay(1000)
                    }
                }
                Text(timeLeft, fontFamily = JetBrainsMono, fontSize = 10.sp, color = Warning)
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge("PENDING")
                Spacer(Modifier.height(8.dp))
                IconButton(
                    onClick = onRevoke,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Revoke",
                        tint = Danger.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
