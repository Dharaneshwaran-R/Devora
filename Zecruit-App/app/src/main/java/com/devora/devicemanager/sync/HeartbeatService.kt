package com.devora.devicemanager.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.devora.devicemanager.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service that sends a heartbeat to the backend every 30 seconds.
 *
 * When the MDM app is uninstalled, Android kills this service immediately,
 * causing heartbeats to stop. The backend's DeviceStatusScheduler then detects
 * the silence after 60 seconds and marks the device INACTIVE.
 *
 * Detection window: ~30–90 seconds after uninstall.
 */
class HeartbeatService : Service() {

    companion object {
        private const val TAG = "HeartbeatService"
        private const val CHANNEL_ID = "devora_heartbeat_channel"
        private const val NOTIFICATION_ID = 2001
        private const val HEARTBEAT_INTERVAL_MS = 30_000L // 30 seconds

        fun start(context: Context) {
            val intent = Intent(context, HeartbeatService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, HeartbeatService::class.java))
        }
    }

    private var heartbeatJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        Log.d(TAG, "HeartbeatService started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startHeartbeatLoop()
        // START_STICKY: if the OS kills the service (low memory), restart it automatically
        return START_STICKY
    }

    override fun onDestroy() {
        heartbeatJob?.cancel()
        Log.d(TAG, "HeartbeatService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            val prefs = applicationContext.getSharedPreferences(
                "devora_enrollment", Context.MODE_PRIVATE
            )
            while (isActive) {
                val deviceId = prefs.getString("device_id", null)
                if (deviceId != null) {
                    try {
                        val response = RetrofitClient.api.sendHeartbeat(deviceId)
                        if (response.isSuccessful) {
                            Log.d(TAG, "Heartbeat OK for $deviceId")
                        } else {
                            Log.w(TAG, "Heartbeat ${response.code()} for $deviceId")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Heartbeat failed (will retry in 30s): ${e.message}")
                    }
                } else {
                    Log.d(TAG, "No device_id in prefs — skipping heartbeat")
                }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DEVORA Device Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps device status active while MDM is running"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_manage)
        .setContentTitle("DEVORA")
        .setContentText("Device monitoring active")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setSilent(true)
        .build()
}
