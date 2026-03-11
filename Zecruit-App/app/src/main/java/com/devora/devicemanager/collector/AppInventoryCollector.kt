package com.devora.devicemanager.collector

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installSource: String
)

object AppInventoryCollector {

    private const val TAG = "AppInventoryCollector"

    fun collect(context: Context): List<AppInfo> {
        val pm = context.packageManager

        val packages: List<PackageInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

        return packages.map { info -> info.toAppInfo(pm) }
    }

    private fun PackageInfo.toAppInfo(pm: PackageManager): AppInfo {
        val appLabel = applicationInfo?.loadLabel(pm)?.toString().orEmpty()

        val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }

        val isSystem = applicationInfo?.let {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } ?: false

        val installer = getInstallerPackage(pm, packageName.orEmpty())

        return AppInfo(
            appName = appLabel,
            packageName = packageName.orEmpty(),
            versionName = versionName.orEmpty(),
            versionCode = versionCode,
            isSystemApp = isSystem,
            installSource = installer
        )
    }

    private fun getInstallerPackage(pm: PackageManager, pkg: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sourceInfo = pm.getInstallSourceInfo(pkg)
                sourceInfo.installingPackageName
                    ?: sourceInfo.initiatingPackageName
                    ?: ""
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(pkg) ?: ""
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not get installer for $pkg: ${e.message}")
            ""
        }
    }
}
