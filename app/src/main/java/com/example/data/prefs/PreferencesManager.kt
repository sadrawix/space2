package com.example.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.plan.PinHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("multiclonix_prefs", Context.MODE_PRIVATE)

    private val _isVip = MutableStateFlow(prefs.getBoolean(KEY_IS_VIP, false))
    val isVip: StateFlow<Boolean> = _isVip.asStateFlow()

    private val _isSpaceLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_SPACE_LOCK, false))
    val isSpaceLockEnabled: StateFlow<Boolean> = _isSpaceLockEnabled.asStateFlow()

    private val _spacePin = MutableStateFlow(prefs.getString(KEY_SPACE_PIN, "") ?: "")
    val spacePin: StateFlow<String> = _spacePin.asStateFlow()

    private val _preferredEngine = MutableStateFlow(prefs.getString(KEY_ENGINE, "AUTO") ?: "AUTO")
    val preferredEngine: StateFlow<String> = _preferredEngine.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setVip(vip: Boolean) {
        prefs.edit().putBoolean(KEY_IS_VIP, vip).apply()
        _isVip.value = vip
    }

    fun setSpaceLock(enabled: Boolean, pin: String) {
        val hashedPin = if (enabled && pin.isNotEmpty()) {
            if (pin.length == 64 && pin.all { it in "0123456789abcdefABCDEF" }) {
                pin
            } else {
                PinHasher.hash(pin)
            }
        } else {
            ""
        }
        prefs.edit()
            .putBoolean(KEY_SPACE_LOCK, enabled)
            .putString(KEY_SPACE_PIN, hashedPin)
            .apply()
        _isSpaceLockEnabled.value = enabled
        _spacePin.value = hashedPin
    }

    fun verifyPin(enteredPin: String): Boolean {
        val stored = _spacePin.value
        if (stored.isEmpty()) return true
        if (enteredPin.isEmpty()) return false

        val standardHash = PinHasher.hash(enteredPin)
        val doubleHash = hashPin(standardHash)
        val plainSha256 = hashPin(enteredPin)

        val isMatch = stored == standardHash ||
                stored == doubleHash ||
                stored == plainSha256 ||
                stored == enteredPin

        if (isMatch && stored != standardHash) {
            // Auto-migrate legacy or double-hashed PIN to standard salted hash
            prefs.edit().putString(KEY_SPACE_PIN, standardHash).apply()
            _spacePin.value = standardHash
        }

        return isMatch
    }

    fun setPreferredEngine(engine: String) {
        prefs.edit().putString(KEY_ENGINE, engine).apply()
        _preferredEngine.value = engine
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    internal fun hashPin(pin: String): String {
        if (pin.isEmpty()) return ""
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            pin
        }
    }

    companion object {
        private const val KEY_IS_VIP = "key_is_vip"
        private const val KEY_SPACE_LOCK = "key_space_lock"
        private const val KEY_SPACE_PIN = "key_space_pin"
        private const val KEY_ENGINE = "key_engine"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_ONBOARDING = "key_onboarding"
    }
}
