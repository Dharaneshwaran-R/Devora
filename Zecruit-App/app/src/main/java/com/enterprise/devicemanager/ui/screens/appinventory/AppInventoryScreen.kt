package com.enterprise.devicemanager.ui.screens.appinventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.devicemanager.data.collector.AppInventoryCollector
import com.enterprise.devicemanager.data.model.AppInventoryItem
import com.enterprise.devicemanager.data.repository.DeviceRepository
import com.enterprise.devicemanager.ui.components.GlassCard
import com.enterprise.devicemanager.ui.theme.MintGreen
import com.enterprise.devicemanager.ui.theme.PillShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppInventoryScreen(isDark: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var apps by remember { mutableStateOf<List<AppInventoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var syncStatus by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    // Collect apps on first composition
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = AppInventoryCollector.collect(context)
        }
        isLoading = false
    }

    val filteredApps = apps.filter { app ->
        when (selectedFilter) {
            "User" -> !app.isSystemApp
            "System" -> app.isSystemApp
            else -> true
        }
    }

    val userCount = apps.count { !it.isSystemApp }
    val systemCount = apps.count { it.isSystemApp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with sync button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "App Inventory",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${apps.size} apps found",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            // Sync to backend button
            FilledIconButton(
                onClick = {
                    scope.launch {
                        isSyncing = true
                        syncStatus = null
                        val repo = DeviceRepository(context)
                        val result = repo.sendAppInventory(apps)
                        syncStatus = if (result.isSuccess) {
                            "Synced ${apps.size} apps"
                        } else {
                            "Sync failed: ${result.exceptionOrNull()?.localizedMessage}"
                        }
                        isSyncing = false
                    }
                },
                enabled = !isSyncing && apps.isNotEmpty(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MintGreen
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync", tint = Color.White)
                }
            }
        }

        // Sync status feedback
        syncStatus?.let { status ->
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (status.startsWith("Synced")) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (status.startsWith("Synced")) MintGreen else Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(status, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$userCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MintGreen)
                    Text("User Apps", fontSize = 11.sp, color = Color.Gray)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$systemCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("System Apps", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf("All" to apps.size, "User" to userCount, "System" to systemCount)
            items(filters) { (filter, count) ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (isSelected) MintGreen else Color.Transparent)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$filter ($count)",
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MintGreen)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredApps) { app ->
                    AppItemCard(app, isDark)
                }
            }
        }
    }
}

@Composable
fun AppItemCard(app: AppInventoryItem, isDark: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // App initial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isSystemApp) Color.Gray.copy(alpha = 0.15f)
                        else MintGreen.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.appName.firstOrNull()?.toString() ?: "?",
                    color = if (app.isSystemApp) Color.Gray else MintGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = if (isDark) Color.White else Color(0xFF1A2332)
                )
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Row {
                    Text(
                        text = "v${app.versionName}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.installSource ?: "Unknown",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // System/User badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (app.isSystemApp) Color.Gray.copy(alpha = 0.1f) else MintGreen.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (app.isSystemApp) "System" else "User",
                    fontSize = 10.sp,
                    color = if (app.isSystemApp) Color.Gray else MintGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
