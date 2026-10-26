package com.example.engine

import android.content.Context
import android.graphics.drawable.Drawable
import com.example.data.model.CloneItem

enum class EngineType { WORK_PROFILE, OEM_DUAL_APPS, VIRTUALIZATION }

data class CloneableApp(
    val packageName: String,
    val appName: String,
    val category: String = "App",
    val isSystemApp: Boolean = false,
    val isAlreadyCloned: Boolean = false,
    val icon: Drawable? = null
)

/**
 * Every engine must be honest:
 * - createClone() returns success ONLY if a real, isolated copy exists.
 * - launchClone() must NEVER fall back to getLaunchIntentForPackage().
 */
interface CloneEngine {
    val engineType: EngineType
    val displayName: String
    val shortName: String
    val description: String
    val isolationLevel: String

    /** How many copies of one app this engine can hold. */
    fun maxInstances(pkg: String): Int
    fun isTrulyIsolated(ctx: Context, pkg: String): Boolean = true

    suspend fun listCloneableApps(ctx: Context): List<CloneableApp>
    suspend fun createClone(ctx: Context, pkg: String, appName: String): Result<CloneItem>
    suspend fun launchClone(ctx: Context, clone: CloneItem): Result<Unit>
}
