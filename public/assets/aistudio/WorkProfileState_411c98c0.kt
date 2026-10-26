package com.example.engine

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.content.pm.LauncherApps

enum class WorkProfileState { UNSUPPORTED, NEEDS_SETUP, READY }

object WorkProfiles {
    fun handle(ctx: Context): UserHandle? {
        val la = ctx.getSystemService(LauncherApps::class.java)
        return la.profiles.firstOrNull { it != Process.myUserHandle() }
    }

    fun state(ctx: Context): WorkProfileState {
        if (!ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS))
            return WorkProfileState.UNSUPPORTED
        return if (handle(ctx) != null) WorkProfileState.READY
        else WorkProfileState.NEEDS_SETUP
    }
}

object OemDualApps {
    private val brands = setOf(
        "samsung", "xiaomi", "redmi", "poco", "huawei", "honor",
        "oppo", "realme", "oneplus", "vivo"
    )

    fun isSupported(): Boolean = Build.MANUFACTURER.lowercase() in brands
}

data class EngineOption(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val hint: String?
)

fun engineOptions(ctx: Context): List<EngineOption> {
    val wp = WorkProfiles.state(ctx)
    val oem = OemDualApps.isSupported()
    return listOf(
        EngineOption("auto", "Auto select (recommended)", true, null),
        EngineOption(
            "oem", "OEM native (Dual Apps)", oem,
            if (oem) null else "Not supported on this phone"
        ),
        EngineOption(
            "workprofile", "Work profile sandbox",
            wp != WorkProfileState.UNSUPPORTED,
            when (wp) {
                WorkProfileState.NEEDS_SETUP -> "Setup required"
                WorkProfileState.UNSUPPORTED -> "Not supported on this phone"
                WorkProfileState.READY -> null
            }
        ),
        EngineOption("virt", "Virtualization engine", false, "Coming soon")
    )
}
