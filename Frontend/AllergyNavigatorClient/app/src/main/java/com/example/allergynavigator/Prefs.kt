package com.example.allergynavigator

import android.content.Context
import android.content.SharedPreferences
import kotlin.apply

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val myPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isOnboardingShown: Boolean
        get() = prefs.getBoolean("onboarding_shown", false)
        set(value) = prefs.edit().putBoolean("onboarding_shown", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "") ?: ""
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userAllergies: Set<String>
        get() = prefs.getStringSet("user_allergies", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("user_allergies", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    fun saveAllergies(allergies: List<String>) {
        prefs.edit().putStringSet("user_allergies", allergies.toSet()).apply()
    }

    fun getAllergies(): Set<String> {
        return prefs.getStringSet("user_allergies", emptySet()) ?: emptySet()
    }
}
