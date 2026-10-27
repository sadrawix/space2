package com.example.engine

import android.content.Context
import android.content.Intent
import com.example.data.model.CloneItem

class VirtualizationEngine : CloneEngine {
    override val engineType: EngineType = EngineType.VIRTUALIZATION
    override val displayName: String = "Isolated Sandbox Engine"
    override val shortName: String = "Sandbox"
    override val description: String = "Filesystem & storage isolated sandbox environment."
    override val isolationLevel: String = "Isolated Sandbox"

    override fun maxInstances(pkg: String): Int = 5

    override fun isTrulyIsolated(ctx: Context, pkg: String): Boolean = true

    override suspend fun listCloneableApps(ctx: Context): List<CloneableApp> {
        val workEngine = WorkProfileEngine()
        return workEngine.listCloneableApps(ctx)
    }

    override suspend fun createClone(
        ctx: Context, pkg: String, appName: String
    ): Result<CloneItem> {
        return IsolatedSandboxManager.createIsolatedInstance(ctx, pkg, appName)
    }

    override suspend fun launchClone(ctx: Context, clone: CloneItem): Result<Unit> {
        return try {
            val pm = ctx.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(clone.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                launchIntent.putExtra("is_isolated_sandbox", true)
                launchIntent.putExtra("sandbox_uuid", clone.sandboxUuid)
                launchIntent.putExtra("sandbox_path", clone.sandboxPath)
                ctx.startActivity(launchIntent)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("No launch intent found for ${clone.packageName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
