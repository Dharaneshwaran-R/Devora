package com.devora.devicemanager.session

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "devora_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_ADMIN_NAME = "admin_name"
    private const val KEY_ADMIN_EMAIL = "admin_email"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, name: String, email: String) {
        prefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ADMIN_NAME, name)
            .putString(KEY_ADMIN_EMAIL, email)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun getAdminName(context: Context): String =
        prefs(context).getString(KEY_ADMIN_NAME, "") ?: ""

    fun getAdminEmail(context: Context): String =
        prefs(context).getString(KEY_ADMIN_EMAIL, "") ?: ""

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
