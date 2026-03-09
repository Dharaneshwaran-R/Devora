package com.devora.devicemanager.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// ══════════════════════════════════════
// DTOs — match the mdm-backend models
// ══════════════════════════════════════

data class EnrollRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("enrollmentToken") val enrollmentToken: String,
    @SerializedName("enrollmentMethod") val enrollmentMethod: String  // "QR_CODE" or "TOKEN"
)

data class EnrollResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("deviceId") val deviceId: String?,
    @SerializedName("enrollmentToken") val enrollmentToken: String?,
    @SerializedName("enrollmentMethod") val enrollmentMethod: String?,
    @SerializedName("enrolledAt") val enrolledAt: String?,
    @SerializedName("status") val status: String?
)

data class DeviceInfoRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("model") val model: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("sdkVersion") val sdkVersion: Int,
    @SerializedName("serialNumber") val serialNumber: String?,
    @SerializedName("imei") val imei: String?,
    @SerializedName("deviceType") val deviceType: String?
)

data class AppInventoryRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("appName") val appName: String,
    @SerializedName("packageName") val packageName: String,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("versionCode") val versionCode: Long,
    @SerializedName("isSystemApp") val isSystemApp: Boolean
)

data class DashboardStats(
    @SerializedName("totalDevices") val totalDevices: Int,
    @SerializedName("activeDevices") val activeDevices: Int,
    @SerializedName(value = "violations", alternate = ["inactiveDevices"]) val violations: Int,
    @SerializedName("totalApps") val totalApps: Int
)

data class DeviceResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("enrollmentToken") val enrollmentToken: String?,
    @SerializedName("enrollmentMethod") val enrollmentMethod: String,
    @SerializedName("enrolledAt") val enrolledAt: String,
    @SerializedName("status") val status: String
)

data class TokenValidationRequest(
    @SerializedName("token") val token: String,
    @SerializedName("deviceId") val deviceId: String
)

data class TokenValidationResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("message") val message: String?
)

data class AdminRegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AdminLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AdminLoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("name") val name: String?,
    @SerializedName("message") val message: String?
)

data class AdminRegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

data class GenerateEnrollmentTokenRequest(
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("type") val type: String
)

data class GenerateEnrollmentTokenResponse(
    @SerializedName("token") val token: String,
    @SerializedName("employeeId") val employeeId: String?,
    @SerializedName("expiresAt") val expiresAt: String?
)

// ══════════════════════════════════════
// RETROFIT API INTERFACE
// ══════════════════════════════════════

interface EnrollmentApiService {

    @POST("api/enrollment/generate")
    suspend fun generateEnrollmentToken(
        @Body request: GenerateEnrollmentTokenRequest
    ): Response<GenerateEnrollmentTokenResponse>

    @POST("api/enroll")
    suspend fun enrollDevice(@Body request: EnrollRequest): Response<EnrollResponse>

    @GET("api/devices/{deviceId}")
    suspend fun getDevice(@Path("deviceId") deviceId: String): Response<EnrollResponse>

    @GET("api/devices")
    suspend fun getAllDevices(): Response<List<EnrollResponse>>

    @GET("api/devices")
    suspend fun getDeviceList(): Response<List<DeviceResponse>>

    @POST("api/device-info")
    suspend fun uploadDeviceInfo(@Body request: DeviceInfoRequest): Response<Unit>

    @POST("api/app-inventory")
    suspend fun uploadAppInventory(@Body request: AppInventoryRequest): Response<Unit>

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @POST("api/admin/register")
    suspend fun registerAdmin(@Body request: AdminRegisterRequest): Response<AdminRegisterResponse>

    @POST("api/admin/login")
    suspend fun loginAdmin(@Body request: AdminLoginRequest): Response<AdminLoginResponse>
}

// ══════════════════════════════════════
// RETROFIT CLIENT SINGLETON
// ══════════════════════════════════════

object RetrofitClient {

    // Default base URL — Railway hosted backend
    private const val DEFAULT_BASE_URL = "https://devora-production-dd2e.up.railway.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: EnrollmentApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EnrollmentApiService::class.java)
    }

    /** Creates a client with a custom base URL (e.g. from Settings screen). */
    fun createWithBaseUrl(baseUrl: String): EnrollmentApiService {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EnrollmentApiService::class.java)
    }
}
