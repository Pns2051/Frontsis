package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class BondhuPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bondhu_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICE_ID = "bondhu_device_id"
        private const val KEY_LANG = "bondhu_lang"
        private const val KEY_DARK_MODE = "bondhu_dark_mode"
        private const val KEY_FIRST_LAUNCH = "bondhu_first_launch"
    }

    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrEmpty()) {
            deviceId = "web-" + UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun isLanguageSet(): Boolean {
        return prefs.contains(KEY_LANG)
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
    }
}
