package com.devora.devicemanager.network.model

import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════
// Enrollment
// ══════════════════════════════════════

data class SyncEnrollRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("model") val model: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("sdkVersion") val sdkVersion: Int
)

data class SyncEnrollResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("deviceId") val deviceId: String?
)

// ══════════════════════════════════════
// Device Info
// ══════════════════════════════════════

data class SyncDeviceInfoRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("model") val model: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("board") val board: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("sdkVersion") val sdkVersion: Int,
    @SerializedName("uniqueId") val uniqueId: String
)

// ══════════════════════════════════════
// App Inventory (bulk upload)
// ══════════════════════════════════════

data class AppInfoDto(
    @SerializedName("appName") val appName: String,
    @SerializedName("packageName") val packageName: String,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("versionCode") val versionCode: Long,
    @SerializedName("isSystemApp") val isSystemApp: Boolean
)

data class BulkAppInventoryRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("apps") val apps: List<AppInfoDto>
)

// ══════════════════════════════════════
// Generic API response
// ══════════════════════════════════════

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
