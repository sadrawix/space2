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
                    virtualizationEngine to "OEM engine not supported; using Isolated Sandbox engine."
                }
            }
            "WORK_PROFILE", "WORKPROFILE" -> {
                if (wpState == WorkProfileState.READY) {
                    workProfileEngine to "Selected by user preference: Android Work Profile sandbox."
                } else {
                    virtualizationEngine to "Work Profile not provisioned; using Isolated Sandbox engine."
                }
            }
            "VIRTUALIZATION", "VIRT", "SANDBOX" -> {
                virtualizationEngine to "Selected by user preference: Isolated Sandbox engine."
            }
            else -> { // AUTO mode
                if (oemSupported) {
                    val mfgName = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                    oemDualAppEngine to "Automatically selected $mfgName Native Dual Space engine."
                } else if (wpState == WorkProfileState.READY) {
                    workProfileEngine to "Android Work Profile is ready and active."
                } else {
                    virtualizationEngine to "Isolated Sandbox Engine (Per-instance storage & database isolation)."
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
