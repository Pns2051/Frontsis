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
        private const val KEY_MODEL = "bondhu_model"
        private const val KEY_THEME = "bondhu_theme"
        private const val KEY_FONT_SIZE = "bondhu_fs"
        private const val KEY_ANIMATIONS = "bondhu_anim"
        private const val KEY_ONBOARDING_DONE = "bondhu_onboarding_done"

        private const val KEY_USER_NAME = "bondhu_user_name"
        private const val KEY_USER_DESC = "bondhu_user_desc"
        private const val KEY_USER_EMAIL = "bondhu_user_email"
        private const val KEY_IS_LOGGED_IN = "bondhu_is_logged_in"
        private const val KEY_LOGIN_TYPE = "bondhu_login_type"

        private const val KEY_CUSTOM_API_ENABLED = "bondhu_custom_api_enabled"
        private const val KEY_CUSTOM_API_ENDPOINT = "bondhu_custom_api_endpoint"
        private const val KEY_CUSTOM_API_KEY = "bondhu_custom_api_key"
        private const val KEY_CUSTOM_API_MODEL = "bondhu_custom_api_model"
        private const val KEY_CACHED_CREDITS = "bondhu_cached_credits"
    }

    // Custom AI API settings
    fun isCustomApiEnabled(): Boolean {
        return prefs.getBoolean(KEY_CUSTOM_API_ENABLED, false)
    }

    fun setCustomApiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CUSTOM_API_ENABLED, enabled).apply()
    }

    fun getCustomApiEndpoint(): String {
        return prefs.getString(KEY_CUSTOM_API_ENDPOINT, "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
    }

    fun setCustomApiEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_CUSTOM_API_ENDPOINT, endpoint.trim()).apply()
    }

    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getCustomApiModel(): String {
        return prefs.getString(KEY_CUSTOM_API_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
    }

    fun setCustomApiModel(model: String) {
        prefs.edit().putString(KEY_CUSTOM_API_MODEL, model.trim()).apply()
    }

    // Cached Credits
    fun getCachedCredits(): Int {
        return prefs.getInt(KEY_CACHED_CREDITS, 50)
    }

    fun setCachedCredits(credits: Int) {
        prefs.edit().putInt(KEY_CACHED_CREDITS, credits).apply()
    }

    // Device ID - generated ONCE ('web-' + random UUID) at guest onboarding, persisted forever
    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrEmpty()) {
            deviceId = "web-" + UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    // Onboarding
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }

    // Language ("bn" | "en", default "bn")
    fun getLanguage(): String {
        return prefs.getString(KEY_LANG, "bn") ?: "bn"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    // AI Model ("light" | "reasoning", default "light")
    fun getModel(): String {
        return prefs.getString(KEY_MODEL, "light") ?: "light"
    }

    fun setModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
    }

    // Theme ("dark" | "light" | "system", default "dark")
    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "dark") ?: "dark"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun isDarkMode(): Boolean {
        val current = getTheme()
        return current == "dark"
    }

    fun setDarkMode(dark: Boolean) {
        setTheme(if (dark) "dark" else "light")
    }

    // Font Size ("small" | "normal" | "large", default "normal")
    fun getFontSize(): String {
        return prefs.getString(KEY_FONT_SIZE, "normal") ?: "normal"
    }

    fun setFontSize(size: String) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply()
    }

    // Animations (default true)
    fun isAnimationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANIMATIONS, true)
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    // User Profile
    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
    }

    fun getUserDesc(): String {
        return prefs.getString(KEY_USER_DESC, "") ?: ""
    }

    fun setUserDesc(desc: String) {
        prefs.edit().putString(KEY_USER_DESC, desc.trim()).apply()
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email.trim()).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun getLoginType(): String {
        return prefs.getString(KEY_LOGIN_TYPE, "guest") ?: "guest"
    }

    fun setLoginType(type: String) {
        prefs.edit().putString(KEY_LOGIN_TYPE, type).apply()
    }

    fun saveProfile(name: String, desc: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_DESC, desc.trim())
            .apply()
    }

    fun logout() {
        val currentDeviceId = getDeviceId()
        prefs.edit().clear().apply()
        // Retain device_id so backend requests remain consistent
        prefs.edit().putString(KEY_DEVICE_ID, currentDeviceId).apply()
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
