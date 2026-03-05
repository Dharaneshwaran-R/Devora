package com.enterprise.devicemanager.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * DeviceAdminReceiver for Device Owner mode.
 * 
 * This receiver handles Device Owner provisioning events including:
 * - QR code-based provisioning during factory reset setup
 * - Token-based enrollment
 * 
 * To provision as Device Owner after factory reset:
 * 1. Factory reset the device
 * 2. Tap 6 times on the welcome screen
 * 3. Scan the QR code containing provisioning extras
 */
class DeviceManagerReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "DeviceManagerReceiver"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceManagerReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
        Toast.makeText(context, "Device Admin Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "Device admin disabled")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Profile provisioning complete - Device Owner mode active")
    }
}
