package com.enterprise.devicemanager.admin

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Helper class for Device Owner mode operations.
 * 
 * QR Code Provisioning:
 * After factory reset, tap 6 times on the welcome screen to open QR scanner.
 * Scan a QR code containing the provisioning JSON below.
 * 
 * The QR code JSON for provisioning:
 * {
 *   "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":
 *     "com.enterprise.devicemanager/com.enterprise.devicemanager.admin.DeviceManagerReceiver",
 *   "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION":
 *     "https://your-server.com/app.apk",
 *   "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": true
 * }
 */
object DeviceOwnerHelper {

    private const val TAG = "DeviceOwnerHelper"

    /**
     * Check if this app is the Device Owner
     */
    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    /**
     * Check if device admin is active
     */
    fun isAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = DeviceManagerReceiver.getComponentName(context)
        return dpm.isAdminActive(componentName)
    }

    /**
     * Get device owner status information
     */
    fun getDeviceOwnerStatus(context: Context): String {
        return when {
            isDeviceOwner(context) -> "Device Owner (Full MDM Control)"
            isAdminActive(context) -> "Device Admin (Limited Control)"
            else -> "Not Provisioned"
        }
    }

    /**
     * Lock the device remotely (requires Device Owner or Admin)
     */
    fun lockDevice(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = DeviceManagerReceiver.getComponentName(context)
            if (dpm.isAdminActive(componentName)) {
                dpm.lockNow()
                true
            } else {
                Log.w(TAG, "Cannot lock: admin not active")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock device", e)
            false
        }
    }

    /**
     * Disable camera (requires Device Owner or Admin)
     */
    fun setCameraDisabled(context: Context, disabled: Boolean): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = DeviceManagerReceiver.getComponentName(context)
            if (dpm.isAdminActive(componentName)) {
                dpm.setCameraDisabled(componentName, disabled)
                true
            } else {
                Log.w(TAG, "Cannot set camera policy: admin not active")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set camera policy", e)
            false
        }
    }

    /**
     * Get the QR code provisioning JSON string.
     * Use this to generate a QR code for device provisioning.
     */
    fun getProvisioningQrJson(apkDownloadUrl: String? = null): String {
        val json = StringBuilder("{")
        json.append("\"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME\":")
        json.append("\"com.enterprise.devicemanager/com.enterprise.devicemanager.admin.DeviceManagerReceiver\"")

        if (!apkDownloadUrl.isNullOrBlank()) {
            json.append(",\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION\":")
            json.append("\"$apkDownloadUrl\"")
        }

        json.append(",\"android.app.extra.PROVISIONING_SKIP_ENCRYPTION\":true")
        json.append("}")
        return json.toString()
    }
}
