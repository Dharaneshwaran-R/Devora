package com.devora.devicemanager.ui.screens.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devora.devicemanager.ui.components.ButtonVariant
import com.devora.devicemanager.ui.components.DevoraButton
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.BgSurface
import com.devora.devicemanager.ui.theme.CardShape
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.DarkBgElevated
import com.devora.devicemanager.ui.theme.DarkBgSurface
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.InputShape
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleBright
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    var adminId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val bgColor = if (isDark) DarkBgBase else Color(0xFFF5F3FF)
    val cardBg = if (isDark) DarkBgSurface else BgSurface
    val inputBg = if (isDark) DarkBgElevated else BgElevated
    val textColor = if (isDark) DarkTextPrimary else TextPrimary

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Theme toggle top-right
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = PurpleCore
                )
            }

            // Login card centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isDark) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = PurpleCore.copy(alpha = 0.25f),
                                    shape = CardShape
                                )
                            } else {
                                Modifier
                            }
                        ),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isDark) 0.dp else 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Canvas logo (smaller version of splash)
                        Canvas(modifier = Modifier.size(52.dp)) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val arcRadius = 20.dp.toPx()

                            drawArc(
                                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                    colors = listOf(PurpleCore, PurpleBright, PurpleCore)
                                ),
                                startAngle = -90f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(centerX - arcRadius, centerY - arcRadius - 3.dp.toPx()),
                                size = Size(arcRadius * 2, arcRadius * 2),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            drawCircle(
                                color = Color(0xFF1E1E32),
                                radius = 2.dp.toPx(),
                                center = Offset(centerX - 6.dp.toPx(), centerY - 4.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFF1E1E32),
                                radius = 2.dp.toPx(),
                                center = Offset(centerX + 6.dp.toPx(), centerY - 4.dp.toPx())
                            )

                            drawRoundRect(
                                color = Color(0xFF1E1E32),
                                topLeft = Offset(centerX - 9.dp.toPx(), centerY + 8.dp.toPx()),
                                size = Size(18.dp.toPx(), 10.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )

                            drawRoundRect(
                                color = PurpleCore,
                                topLeft = Offset(centerX - 1.5.dp.toPx(), centerY + 8.dp.toPx()),
                                size = Size(3.dp.toPx(), 7.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx())
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. "DEVORA"
                        Text(
                            text = "DEVORA",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = PurpleCore
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 3. "Enterprise Device Manager"
                        Text(
                            text = "Enterprise Device Manager",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4. Divider
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = PurpleCore.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 5. Admin ID label
                        Text(
                            text = "Admin ID",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = PurpleCore,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6. Admin ID TextField
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(inputBg, InputShape)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = PurpleCore,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (adminId.isEmpty()) {
                                        Text(
                                            text = "Enter admin ID",
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = TextMuted
                                        )
                                    }
                                    BasicTextField(
                                        value = adminId,
                                        onValueChange = { adminId = it },
                                        textStyle = TextStyle(
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = textColor
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 7. Password label
                        Text(
                            text = "Password",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = PurpleCore,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 8. Password TextField
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(inputBg, InputShape)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = PurpleCore,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (password.isEmpty()) {
                                        Text(
                                            text = "Enter password",
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = TextMuted
                                        )
                                    }
                                    BasicTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        textStyle = TextStyle(
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = textColor
                                        ),
                                        singleLine = true,
                                        visualTransformation = if (showPassword) {
                                            VisualTransformation.None
                                        } else {
                                            PasswordVisualTransformation()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = "Toggle password visibility",
                                        tint = PurpleCore,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 9. Sign In button
                        DevoraButton(
                            text = "Sign In",
                            onClick = {
                                isLoading = true
                                if (adminId == "admin" && password == "admin123") {
                                    onLoginSuccess()
                                } else {
                                    isLoading = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Invalid credentials")
                                    }
                                }
                            },
                            variant = ButtonVariant.PRIMARY,
                            isLoading = isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 10. Forgot credentials
                        Text(
                            text = "Forgot credentials? Contact IT Admin",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom: "Secured by Enterprise MDM"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secured by Enterprise MDM",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
