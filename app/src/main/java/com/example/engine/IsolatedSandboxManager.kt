package com.example.engine

import android.content.Context
import android.util.Log
import com.example.data.model.CloneItem
import java.io.File
import java.util.UUID

/**
 * Manages completely isolated directory environments:
 * - Root path: /data/user/0/<app_package>/files/sandboxes/<UUID>/
 * - Subdirectories:
 *     - shared_prefs/ : Dedicated, isolated SharedPreferences files
 *     - databases/    : Dedicated, isolated SQLite/Room databases
 *     - files/        : Internal isolated filesystem storage
 *     - cache/        : Temporary isolated cache directory
 */
object IsolatedSandboxManager {
    private const val TAG = "IsolatedSandbox"
    const val SANDBOX_ROOT_DIR = "sandboxes"

    /**
     * Creates a fully isolated instance directory environment:
     * 1. Generates a unique UUID
     * 2. Creates protected directory at /data/user/0/<app_package>/files/sandboxes/<UUID>/
     * 3. Creates dedicated directories for shared_prefs and databases (exclusive to this instance)
     * 4. Returns the created CloneItem populated with sandbox details
     */
    fun createIsolatedInstance(
        context: Context,
        packageName: String,
        appName: String,
        instanceIndex: Int = 2
    ): Result<CloneItem> {
        return try {
            val uuid = UUID.randomUUID().toString()
            val sandboxesDir = File(context.filesDir, SANDBOX_ROOT_DIR)
            if (!sandboxesDir.exists()) {
                sandboxesDir.mkdirs()
            }

            val instanceDir = File(sandboxesDir, uuid)
            instanceDir.mkdirs()

            // 4. Dedicated directories for shared_prefs and databases (exclusive to this instance)
            val sharedPrefsDir = File(instanceDir, "shared_prefs")
            val databasesDir = File(instanceDir, "databases")
            val internalFilesDir = File(instanceDir, "files")
            val cacheDir = File(instanceDir, "cache")

            sharedPrefsDir.mkdirs()
            databasesDir.mkdirs()
            internalFilesDir.mkdirs()
            cacheDir.mkdirs()

            // Ensure permissions are strictly protected to this app user
            instanceDir.setReadable(true, true)
            instanceDir.setWritable(true, true)
            instanceDir.setExecutable(true, true)

            // Write metadata descriptor inside the sandbox
            val metaFile = File(instanceDir, "sandbox_meta.json")
            val metaContent = """
                {
                  "uuid": "$uuid",
                  "packageName": "$packageName",
                  "appName": "$appName",
                  "instanceIndex": $instanceIndex,
                  "createdAt": ${System.currentTimeMillis()},
                  "isolationType": "OS_USER_SANDBOX",
                  "paths": {
                    "root": "${instanceDir.absolutePath}",
                    "shared_prefs": "${sharedPrefsDir.absolutePath}",
                    "databases": "${databasesDir.absolutePath}",
                    "files": "${internalFilesDir.absolutePath}",
                    "cache": "${cacheDir.absolutePath}"
                  }
                }
            """.trimIndent()
            metaFile.writeText(metaContent)

            val clone = CloneItem(
                packageName = packageName,
                originalAppName = appName,
                displayName = "$appName $instanceIndex",
                engineType = EngineType.VIRTUALIZATION.name,
                instanceIndex = instanceIndex,
                badgeLabel = "CLONE",
                badgeColorHex = "#2ED573",
                sandboxUuid = uuid,
                sandboxPath = instanceDir.absolutePath
            )

            Log.i(TAG, "Created isolated sandbox at ${instanceDir.absolutePath} for $packageName (UUID: $uuid)")
            Result.success(clone)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create isolated instance for $packageName", e)
            Result.failure(e)
        }
    }

    /**
     * Completely cleans up the isolated instance sandbox directory
     */
    fun deleteIsolatedInstance(context: Context, sandboxUuid: String, sandboxPath: String) {
        try {
            val target = when {
                sandboxPath.isNotEmpty() -> File(sandboxPath)
                sandboxUuid.isNotEmpty() -> File(context.filesDir, "$SANDBOX_ROOT_DIR/$sandboxUuid")
                else -> null
            }
            if (target != null && target.exists()) {
                target.deleteRecursively()
                Log.i(TAG, "Cleaned up isolated sandbox directory: ${target.absolutePath}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete sandbox directory for UUID: $sandboxUuid", e)
        }
    }
}
