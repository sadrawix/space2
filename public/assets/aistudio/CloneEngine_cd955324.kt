package com.example.engine

import android.content.Context
import com.example.data.model.CloneItem

enum class EngineType { WORK_PROFILE, OEM_DUAL_APPS, VIRTUALIZATION }

/**
 * Every engine must be honest:
 * - createClone() returns success ONLY if a real, isolated copy exists.
 * - launchClone() must NEVER fall back to getLaunchIntentForPackage().
 */
interface CloneEngine {
    val engineType: EngineType

    /** How many copies of one app this engine can hold. */
    fun maxInstances(pkg: String): Int

    suspend fun listCloneableApps(ctx: Context): List<CloneableApp>
    suspend fun createClone(ctx: Context, pkg: String, appName: String): Result<CloneItem>
    suspend fun launchClone(ctx: Context, clone: CloneItem): Result<Unit>
}
