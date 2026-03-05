package com.enterprise.devicemanager.data.collector

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.enterprise.devicemanager.data.model.AppInventoryItem

/**
 * Collects the list of installed applications using PackageManager APIs.
 *
 * Collects:
 * - Application name
 * - Package name
 * - Version name & version code
 * - Installation source
 * - Classification as system app or user-installed app
 */
object AppInventoryCollector {

    private const val TAG = "AppInventoryCollector"

    /**
     * Collect all installed applications on the device.
     * @param includeSystemApps Whether to include system apps (default: true)
     */
    fun collect(context: Context, includeSystemApps: Boolean = true): List<AppInventoryItem> {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get installed packages", e)
            emptyList()
        }

        val apps = packages.mapNotNull { packageInfo ->
            try {
                val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                if (!includeSystemApps && isSystemApp) {
                    return@mapNotNull null
                }

                val appName = pm.getApplicationLabel(appInfo).toString()
                val packageName = packageInfo.packageName
                val versionName = packageInfo.versionName ?: "Unknown"
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
                val installSource = getInstallSource(pm, packageName)

                AppInventoryItem(
                    appName = appName,
                    packageName = packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    installSource = installSource,
                    isSystemApp = isSystemApp
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to collect info for package: ${packageInfo.packageName}", e)
                null
            }
        }.sortedWith(compareBy({ it.isSystemApp }, { it.appName.lowercase() }))

        Log.i(TAG, "Collected ${apps.size} apps (${apps.count { !it.isSystemApp }} user, ${apps.count { it.isSystemApp }} system)")
        return apps
    }

    /**
     * Get the installation source of a package.
     * Returns the installer package name (e.g., "com.android.vending" for Play Store).
     */
    private fun getInstallSource(pm: PackageManager, packageName: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sourceInfo = pm.getInstallSourceInfo(packageName)
                sourceInfo.installingPackageName ?: sourceInfo.initiatingPackageName ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName) ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
