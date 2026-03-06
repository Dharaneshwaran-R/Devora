package com.devora.devicemanager.ui.screens.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devora.devicemanager.ui.components.DevoraBottomNav
import com.devora.devicemanager.ui.components.DevoraCard
import com.devora.devicemanager.ui.components.StatusBadge
import com.devora.devicemanager.ui.theme.BgBase
import com.devora.devicemanager.ui.theme.BgElevated
import com.devora.devicemanager.ui.theme.BgSurface
import com.devora.devicemanager.ui.theme.ButtonShape
import com.devora.devicemanager.ui.theme.ChipShape
import com.devora.devicemanager.ui.theme.DMSans
import com.devora.devicemanager.ui.theme.Danger
import com.devora.devicemanager.ui.theme.DarkBgBase
import com.devora.devicemanager.ui.theme.DarkBgSurface
import com.devora.devicemanager.ui.theme.DarkTextPrimary
import com.devora.devicemanager.ui.theme.JetBrainsMono
import com.devora.devicemanager.ui.theme.PlusJakartaSans
import com.devora.devicemanager.ui.theme.PurpleBorder
import com.devora.devicemanager.ui.theme.PurpleCore
import com.devora.devicemanager.ui.theme.PurpleDeep
import com.devora.devicemanager.ui.theme.Success
import com.devora.devicemanager.ui.theme.TextMuted
import com.devora.devicemanager.ui.theme.TextPrimary

// ══════════════════════════════════════
// DEVICE DATA
// ══════════════════════════════════════

data class Device(
    val name: String,
    val manufacturer: String,
    val model: String,
    val status: String,
    val api: String,
    val initial: String
)

val mockDevices = listOf(
    Device("Galaxy S24 Ultra", "Samsung", "SM-S928B", "ONLINE", "API 34", "G"),
    Device("Pixel 8 Pro", "Google", "GC3VE", "ONLINE", "API 34", "P"),
    Device("Xperia 1 V", "Sony", "XQ-DQ72", "OFFLINE", "API 33", "X"),
    Device("ThinkPhone", "Motorola", "XT2309-2", "FLAGGED", "API 33", "T"),
    Device("Galaxy Tab S9", "Samsung", "SM-X710", "ONLINE", "API 34", "G"),
    Device("Nokia G42", "HMD Global", "TA-1581", "OFFLINE", "API 33", "N"),
    Device("Pixel Tablet", "Google", "GTU8P", "ONLINE", "API 34", "P"),
    Device("TC52ax", "Zebra", "TC520L", "FLAGGED", "API 30", "T")
)

// ══════════════════════════════════════
// DEVICE LIST SCREEN
// ══════════════════════════════════════

@Composable
fun DeviceListScreen(
    onDeviceClick: (String) -> Unit,
    onEnrollClick: () -> Unit,
    onNavigate: (String) -> Unit,
    isDark: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "ONLINE", "OFFLINE", "FLAGGED")

    val bgColor = if (isDark) DarkBgBase else BgBase
    val textColor = if (isDark) DarkTextPrimary else TextPrimary
    val surfaceBg = if (isDark) DarkBgSurface else BgSurface

    val filteredDevices = mockDevices.filter { device ->
        val matchesSearch = device.name.contains(searchQuery, ignoreCase = true) ||
                device.manufacturer.contains(searchQuery, ignoreCase = true) ||
                device.model.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "ALL" || device.status == selectedFilter
        matchesSearch && matchesFilter
    }

    Scaffold(
        bottomBar = {
            DevoraBottomNav(
                currentRoute = "device_list",
                onNavigate = onNavigate,
                isDark = isDark
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEnrollClick,
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PurpleCore, PurpleDeep)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Enroll device",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Devices",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter",
                    tint = PurpleCore,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(surfaceBg, ButtonShape)
                    .border(1.dp, PurpleBorder, ButtonShape)
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
                                text = "Search devices...",
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { searchQuery = "" }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) PurpleCore.copy(alpha = 0.12f) else surfaceBg,
                                ChipShape
                            )
                            .border(
                                1.dp,
                                if (isSelected) PurpleCore.copy(alpha = 0.40f) else PurpleCore.copy(alpha = 0.15f),
                                ChipShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedFilter = filter }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontFamily = if (isSelected) PlusJakartaSans else DMSans,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) PurpleCore else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Device list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredDevices) { device ->
                    val stripColor = when (device.status) {
                        "ONLINE" -> Success
                        "FLAGGED" -> Danger
                        else -> TextMuted
                    }

                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onDeviceClick(device.name) }
                        )
                    ) {
                        DevoraCard(accentColor = stripColor, isDark = isDark) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (device.status == "ONLINE") {
                                                Brush.linearGradient(
                                                    listOf(PurpleCore, PurpleDeep)
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        if (isDark) com.devora.devicemanager.ui.theme.DarkBgElevated else BgElevated,
                                                        if (isDark) com.devora.devicemanager.ui.theme.DarkBgElevated else BgElevated
                                                    )
                                                )
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = device.initial,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (device.status == "ONLINE") Color.White else TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Device info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${device.manufacturer} · ${device.model}",
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Last seen 2m ago",
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Right column
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    StatusBadge(status = device.status)
                                    Text(
                                        text = device.api,
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
