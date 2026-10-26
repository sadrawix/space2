package com.example.plan

import com.example.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest

/** Free: 3 apps. VIP: unlimited apps. Counted per distinct app, not per copy. */
object PlanPolicy {
    const val FREE_MAX_APPS = 3

    fun maxApps(isVip: Boolean): Int = if (isVip) Int.MAX_VALUE else FREE_MAX_APPS
}

/** Swap this implementation for a Play Billing one later. Nothing else changes. */
interface VipProvider {
    val isVip: StateFlow<Boolean>
}

class LocalVipProvider(prefs: PreferencesManager) : VipProvider {
    override val isVip: StateFlow<Boolean> = prefs.isVip
}

/** Store only a salted hash of the PIN, never the PIN itself. */
object PinHasher {
    private const val SALT = "multiclonix.v1"

    fun hash(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((SALT + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
