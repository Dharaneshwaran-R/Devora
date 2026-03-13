package com.devora.devicemanager.sync

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
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
import com.devora.devicemanager.network.DeviceAppRestrictionResponse
import com.devora.devicemanager.network.RetrofitClient
import java.util.concurrent.TimeUnit

/**
 * Periodic worker (every 15 min — WorkManager minimum) that enforces MDM policies:
 *  1. App restrictions   — setPackagesSuspended() per restricted app
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
        if (!dpm.isDeviceOwnerApp(applicationContext.packageName)) {
            Log.w(TAG, "Not device owner, skipping app restrictions")
            return
        }

        val response = RetrofitClient.api.getAllAppRestrictions(deviceId)
        if (!response.isSuccessful) return

        val restrictions = response.body() ?: emptyList()
        applyAppRestrictions(dpm, admin, restrictions)
    }

    private fun applyAppRestrictions(
        dpm: DevicePolicyManager,
        adminComponent: android.content.ComponentName,
        restrictedApps: List<DeviceAppRestrictionResponse>
    ) {
        if (!dpm.isDeviceOwnerApp(applicationContext.packageName)) {
            Log.w(TAG, "Not device owner, skipping")
            return
        }

        val restrictionPrefs = applicationContext.getSharedPreferences("devora_restrictions", Context.MODE_PRIVATE)
        val previousRestricted = restrictionPrefs.getStringSet("restricted_packages", emptySet()) ?: emptySet()

        val restrictedNow = restrictedApps
            .filter { it.restricted }
            .map { it.packageName }
            .toSet()

        val explicitUnsuspend = restrictedApps
            .filter { !it.restricted }
            .map { it.packageName }
            .toSet()

        val toSuspend = restrictedNow.toTypedArray()
        val toUnsuspend = (explicitUnsuspend + (previousRestricted - restrictedNow)).toTypedArray()

        if (toSuspend.isNotEmpty()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Call the richer overload via reflection when available on this API/SDK level.
                    try {
                        val suspendDialogInfoClass = Class.forName("android.content.pm.SuspendDialogInfo")
                        val builderClass = Class.forName("android.content.pm.SuspendDialogInfo\$Builder")
                        val builder = builderClass.getDeclaredConstructor().newInstance()
                        builderClass.getMethod("setTitle", String::class.java)
                            .invoke(builder, "App Restricted")
                        builderClass.getMethod("setMessage", String::class.java)
                            .invoke(builder, "This app has been restricted by your IT administrator. Contact your admin for access.")
                        val dialogInfo = builderClass.getMethod("build").invoke(builder)

                        val method = DevicePolicyManager::class.java.getMethod(
                            "setPackagesSuspended",
                            android.content.ComponentName::class.java,
                            Array<String>::class.java,
                            Boolean::class.javaPrimitiveType,
                            android.os.PersistableBundle::class.java,
                            android.os.PersistableBundle::class.java,
                            suspendDialogInfoClass
                        )
                        method.invoke(dpm, adminComponent, toSuspend, true, null, null, dialogInfo)
                    } catch (_: Exception) {
                        dpm.setPackagesSuspended(adminComponent, toSuspend, true)
                    }
                } else {
                    dpm.setPackagesSuspended(adminComponent, toSuspend, true)
                }
                Log.d(TAG, "Suspended ${toSuspend.size} app(s)")
            } catch (e: Exception) {
                Log.e(TAG, "Suspend failed: ${e.message}")
            }
        }

        if (toUnsuspend.isNotEmpty()) {
            try {
                dpm.setPackagesSuspended(adminComponent, toUnsuspend, false)
                Log.d(TAG, "Unsuspended ${toUnsuspend.size} app(s)")
            } catch (e: Exception) {
                Log.e(TAG, "Unsuspend failed: ${e.message}")
            }
        }

        restrictionPrefs.edit().putStringSet("restricted_packages", restrictedNow).apply()
    }

    private suspend fun enforcePolicies(
        deviceId: String,
        dpm: DevicePolicyManager,
        admin: android.content.ComponentName
    ) {
        if (!dpm.isDeviceOwnerApp(applicationContext.packageName)) {
            Log.w(TAG, "Not device owner, skipping policy enforcement")
            return
        }

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
        if (!dpm.isDeviceOwnerApp(applicationContext.packageName)) {
            Log.w(TAG, "Not device owner, skipping command execution")
            return
        }

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
