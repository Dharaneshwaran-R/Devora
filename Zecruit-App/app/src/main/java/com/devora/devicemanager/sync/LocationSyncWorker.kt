package com.devora.devicemanager.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.devora.devicemanager.network.LocationReportRequest
import com.devora.devicemanager.network.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Periodic worker (every 15 min) that reports GPS location to the backend.
 * Only reports if locationTrackingEnabled in device policies and location permission granted.
 */
class LocationSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LocationSyncWorker"
        private const val WORK_NAME = "devora_location_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<LocationSyncWorker>(
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
            Log.d(TAG, "Location sync worker scheduled")
        }
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("devora_enrollment", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null) ?: return Result.success()

        // Check if location tracking is enabled in policies
        try {
            val policyResponse = RetrofitClient.api.getDevicePolicies(deviceId)
            if (policyResponse.isSuccessful) {
                val policy = policyResponse.body()
                if (policy != null && !policy.locationTrackingEnabled) {
                    Log.d(TAG, "Location tracking disabled by policy")
                    return Result.success()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check policy: ${e.message}")
        }

        // Check permission
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted")
            return Result.success()
        }

        // Get current location
        try {
            val location = getCurrentLocation() ?: run {
                Log.w(TAG, "Failed to get location")
                return Result.success()
            }

            val request = LocationReportRequest(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy
            )

            val response = RetrofitClient.api.reportLocation(deviceId, request)
            if (response.isSuccessful) {
                Log.d(TAG, "Location reported: ${location.latitude}, ${location.longitude}")
            } else {
                Log.w(TAG, "Location report failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Location sync failed: ${e.message}")
        }

        return Result.success()
    }

    @Suppress("MissingPermission")
    private suspend fun getCurrentLocation(): android.location.Location? {
        return suspendCancellableCoroutine { cont ->
            val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }

            cont.invokeOnCancellation { cts.cancel() }
        }
    }
}
