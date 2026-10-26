package com.example.engine

import android.content.Context
import android.os.Build

data class EngineSelectionInfo(
    val activeEngine: CloneEngine,
    val reason: String,
    val isOemSupported: Boolean,
    val isWorkProfileSupported: Boolean,
    val manufacturer: String = Build.MANUFACTURER
)

class CloneEngineManager(private val context: Context) {
    val workProfileEngine = WorkProfileEngine()
    val oemDualAppEngine = OemDualAppEngine()
    val virtualizationEngine = VirtualizationEngine()

    fun getEngineByType(type: EngineType): CloneEngine {
        return when (type) {
            EngineType.WORK_PROFILE -> workProfileEngine
            EngineType.OEM_DUAL_APPS -> oemDualAppEngine
            EngineType.VIRTUALIZATION -> virtualizationEngine
        }
    }

    fun selectActiveEngine(userPreference: String = "AUTO"): EngineSelectionInfo {
        val oemSupported = oemDualAppEngine.isSupported(context)
        val wpState = WorkProfiles.state(context)
        val wpSupported = wpState != WorkProfileState.UNSUPPORTED

        val (engine, reason) = when (userPreference.uppercase()) {
            "OEM_DUAL_APPS", "OEM_DUAL_APP", "OEM" -> {
                if (oemSupported) {
                    oemDualAppEngine to "Selected by user preference: native ${Build.MANUFACTURER.uppercase()} engine."
                } else {
                    workProfileEngine to "OEM engine requested but device not supporting it; falling back to Work Profile."
                }
            }
            "WORK_PROFILE", "WORKPROFILE" -> {
                workProfileEngine to "Selected by user preference: Android Work Profile sandbox."
            }
            "VIRTUALIZATION", "VIRT" -> {
                virtualizationEngine to "Selected by user preference: experimental sandboxed virtualization."
            }
            else -> { // AUTO mode
                if (oemSupported) {
                    val mfgName = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                    oemDualAppEngine to "Automatically selected $mfgName Native Dual Space engine."
                } else if (wpState == WorkProfileState.READY) {
                    workProfileEngine to "Android Work Profile is ready and active."
                } else if (wpSupported) {
                    workProfileEngine to "Android Work Profile sandbox for OS-level isolation."
                } else {
                    virtualizationEngine to "Selected Virtualization Engine."
                }
            }
        }

        return EngineSelectionInfo(
            activeEngine = engine,
            reason = reason,
            isOemSupported = oemSupported,
            isWorkProfileSupported = wpSupported
        )
    }
}
