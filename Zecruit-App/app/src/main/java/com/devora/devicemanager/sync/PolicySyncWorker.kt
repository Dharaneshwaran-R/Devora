package com.devora.devicemanager.sync

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.UserManager
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.devora.devicemanager.AdminReceiver
import com.devora.devicemanager.network.RetrofitClient
import java.util.concurrent.TimeUnit

/**
 * Periodic worker (every 15 min — WorkManager minimum) that enforces MDM policies:
 *  1. App restrictions   — setApplicationHidden() per restricted app
 *  2. Camera policy      — setCameraDisabled()
 *  3. Install/uninstall  — addUserRestriction(DISALLOW_INSTALL/UNINSTALL_APPS)
 *  4. Pending commands   — LOCK → lockNow(), WIPE → wipeData(), CAMERA_* → setCameraDisabled()
 */
class PolicySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PolicySyncWorker"
        private const val WORK_NAME = "devora_policy_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PolicySyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Policy sync worker scheduled")
        }
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("devora_enrollment", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null) ?: return Result.success()

        val dpm = applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!dpm.isDeviceOwnerApp(applicationContext.packageName)) {
            Log.d(TAG, "Not Device Owner — skipping policy sync")
            return Result.success()
        }

        val admin = AdminReceiver.getComponentName(applicationContext)

        try {
            enforceAppRestrictions(deviceId, dpm, admin)
        } catch (e: Exception) {
            Log.w(TAG, "App restriction enforcement failed: ${e.message}")
        }

        try {
            enforcePolicies(deviceId, dpm, admin)
        } catch (e: Exception) {
            Log.w(TAG, "Policy enforcement failed: ${e.message}")
        }

        try {
            executePendingCommands(deviceId, dpm, admin)
        } catch (e: Exception) {
            Log.w(TAG, "Command execution failed: ${e.message}")
        }

        return Result.success()
    }

    private suspend fun enforceAppRestrictions(
        deviceId: String,
        dpm: DevicePolicyManager,
        admin: android.content.ComponentName
    ) {
        val response = RetrofitClient.api.getAllAppRestrictions(deviceId)
        if (!response.isSuccessful) return

        val restrictions = response.body() ?: emptyList()

        // Hide restricted apps
        val restrictedPackages = mutableSetOf<String>()
        for (r in restrictions) {
            if (r.restricted) {
                restrictedPackages.add(r.packageName)
                val hidden = dpm.setApplicationHidden(admin, r.packageName, true)
                if (hidden) {
                    Log.d(TAG, "Hidden app: ${r.packageName}")
                } else {
                    Log.w(TAG, "Failed to hide: ${r.packageName}")
                }
            } else {
                // Unhide apps that are no longer restricted
                dpm.setApplicationHidden(admin, r.packageName, false)
            }
        }

        // Unhide previously restricted apps that are no longer in the list
        val restrictionPrefs = applicationContext.getSharedPreferences("devora_restrictions", Context.MODE_PRIVATE)
        val previousRestricted = restrictionPrefs.getStringSet("restricted_packages", emptySet()) ?: emptySet()
        val toUnhide = previousRestricted - restrictedPackages
        for (pkg in toUnhide) {
            dpm.setApplicationHidden(admin, pkg, false)
            Log.d(TAG, "Unhidden app: $pkg")
        }

        restrictionPrefs.edit().putStringSet("restricted_packages", restrictedPackages).apply()
    }

    private suspend fun enforcePolicies(
        deviceId: String,
        dpm: DevicePolicyManager,
        admin: android.content.ComponentName
    ) {
        val response = RetrofitClient.api.getDevicePolicies(deviceId)
        if (!response.isSuccessful) return

        val policy = response.body() ?: return

        // Camera
        dpm.setCameraDisabled(admin, policy.cameraDisabled)
        Log.d(TAG, "Camera disabled: ${policy.cameraDisabled}")

        // Install/uninstall restrictions
        if (policy.installBlocked) {
            dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
        } else {
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
        }

        if (policy.uninstallBlocked) {
            dpm.addUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS)
        } else {
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS)
        }

        Log.d(TAG, "Install blocked: ${policy.installBlocked}, Uninstall blocked: ${policy.uninstallBlocked}")
    }

    private suspend fun executePendingCommands(
        deviceId: String,
        dpm: DevicePolicyManager,
        admin: android.content.ComponentName
    ) {
        val response = RetrofitClient.api.getPendingCommands(deviceId)
        if (!response.isSuccessful) return

        val commands = response.body() ?: emptyList()
        for (cmd in commands) {
            when (cmd.commandType) {
                "LOCK" -> {
                    dpm.lockNow()
                    Log.d(TAG, "Executed LOCK command ${cmd.id}")
                }
                "WIPE" -> {
                    // Acknowledge before wiping since device resets
                    try {
                        RetrofitClient.api.ackCommand(deviceId, cmd.id)
                    } catch (_: Exception) { }
                    dpm.wipeData(0)
                    return // Device is wiping, no further commands
                }
                "CAMERA_DISABLE" -> {
                    dpm.setCameraDisabled(admin, true)
                    Log.d(TAG, "Executed CAMERA_DISABLE command ${cmd.id}")
                }
                "CAMERA_ENABLE" -> {
                    dpm.setCameraDisabled(admin, false)
                    Log.d(TAG, "Executed CAMERA_ENABLE command ${cmd.id}")
                }
            }
            // Acknowledge command execution
            try {
                RetrofitClient.api.ackCommand(deviceId, cmd.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to ack command ${cmd.id}: ${e.message}")
            }
        }
    }
}
