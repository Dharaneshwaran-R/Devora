package com.devora.devicemanager.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.devora.devicemanager.AdminReceiver
import com.devora.devicemanager.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that sends a heartbeat to the backend every 60 seconds.
 * When the MDM app is uninstalled this service stops, and the backend scheduler
 * marks the device INACTIVE after 3 minutes of missing heartbeats.
 */
class HeartbeatService : Service() {

    companion object {
        private const val TAG = "HeartbeatService"
        private const val CHANNEL_ID = "devora_heartbeat_channel"
        private const val NOTIFICATION_ID = 2001
        private const val HEARTBEAT_INTERVAL_MS = 60_000L

        fun start(context: Context) {
            val intent = Intent(context, HeartbeatService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, HeartbeatService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var heartbeatJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Devora MDM")
            .setContentText("Device monitoring active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            val prefs = getSharedPreferences("devora_enrollment", Context.MODE_PRIVATE)
            while (true) {
                val deviceId = prefs.getString("device_id", null)
                if (deviceId != null) {
                    try {
                        RetrofitClient.api.sendHeartbeat(deviceId)
                        Log.d(TAG, "Heartbeat sent for $deviceId")
                    } catch (e: Exception) {
                        Log.w(TAG, "Heartbeat failed: ${e.message}")
                    }

                    // Enforce app restrictions from backend
                    enforceAppRestrictions(deviceId)
                }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Device Monitoring",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps device monitoring active in background"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /**
     * Fetches restricted apps from backend and suspends them via DevicePolicyManager.
     * Only works when the app is Device Owner.
     */
    private suspend fun enforceAppRestrictions(deviceId: String) {
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (!dpm.isDeviceOwnerApp(packageName)) {
                Log.d(TAG, "Not Device Owner — skipping app restriction enforcement")
                return
            }

            val admin = AdminReceiver.getComponentName(this@HeartbeatService)
            val response = RetrofitClient.api.getRestrictedApps(deviceId)
            if (!response.isSuccessful) return

            val restrictedPackages = (response.body() ?: emptyList()).map { it.packageName }.toTypedArray()

            if (restrictedPackages.isNotEmpty()) {
                val failed = dpm.setPackagesSuspended(admin, restrictedPackages, true)
                if (failed.isEmpty()) {
                    Log.d(TAG, "Suspended ${restrictedPackages.size} apps")
                } else {
                    Log.w(TAG, "Failed to suspend: ${failed.joinToString()}")
                }
            }

            // Unsuspend previously restricted apps that are no longer in the list
            val prefs = getSharedPreferences("devora_restrictions", Context.MODE_PRIVATE)
            val previousRestricted = prefs.getStringSet("restricted_packages", emptySet()) ?: emptySet()
            val currentSet = restrictedPackages.toSet()
            val toUnsuspend = previousRestricted - currentSet
            if (toUnsuspend.isNotEmpty()) {
                dpm.setPackagesSuspended(admin, toUnsuspend.toTypedArray(), false)
                Log.d(TAG, "Unsuspended ${toUnsuspend.size} apps")
            }

            prefs.edit().putStringSet("restricted_packages", currentSet).apply()
        } catch (e: Exception) {
            Log.w(TAG, "App restriction enforcement failed: ${e.message}")
        }
    }
}
