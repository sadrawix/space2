package com.example.engine

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.content.pm.LauncherApps

enum class WorkProfileState { UNSUPPORTED, NEEDS_SETUP, READY }

object WorkProfiles {
    fun handle(ctx: Context): UserHandle? {
        return try {
            val la = ctx.getSystemService(LauncherApps::class.java)
            val laProfile = la?.profiles?.firstOrNull { it != Process.myUserHandle() }
            if (laProfile != null) return laProfile

            val um = ctx.getSystemService(UserManager::class.java)
            um?.userProfiles?.firstOrNull { it != Process.myUserHandle() }
        } catch (e: Exception) {
            null
        }
    }

    fun hasAnyProfile(ctx: Context): Boolean {
        return try {
            val um = ctx.getSystemService(UserManager::class.java)
            val umProfiles = um?.userProfiles ?: emptyList()
            if (umProfiles.any { it != Process.myUserHandle() }) return true

            val la = ctx.getSystemService(LauncherApps::class.java)
            val laProfiles = la?.profiles ?: emptyList()
            laProfiles.any { it != Process.myUserHandle() }
        } catch (e: Exception) {
            false
        }
    }

    fun state(ctx: Context): WorkProfileState {
        return try {
            val pm = ctx.packageManager ?: return WorkProfileState.UNSUPPORTED
            if (!pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS))
                return WorkProfileState.UNSUPPORTED
            return if (handle(ctx) != null) WorkProfileState.READY
            else WorkProfileState.NEEDS_SETUP
        } catch (e: Exception) {
            WorkProfileState.UNSUPPORTED
        }
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
