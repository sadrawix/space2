package com.example.engine

import android.content.Context
import com.example.data.model.CloneItem

class VirtualizationEngine : CloneEngine {
    override val engineType: EngineType = EngineType.VIRTUALIZATION
    override val displayName: String = "Virtualization Engine"
    override val shortName: String = "Virtual"
    override val description: String = "Experimental sandboxed virtualization engine."
    override val isolationLevel: String = "Sandbox"

    override fun maxInstances(pkg: String): Int = 3

    override fun isTrulyIsolated(ctx: Context, pkg: String): Boolean = false

    override suspend fun listCloneableApps(ctx: Context): List<CloneableApp> {
        val workEngine = WorkProfileEngine()
        return workEngine.listCloneableApps(ctx)
    }

    override suspend fun createClone(
        ctx: Context, pkg: String, appName: String
    ): Result<CloneItem> {
        return Result.failure(IllegalStateException("Virtualization engine coming soon"))
    }

    override suspend fun launchClone(ctx: Context, clone: CloneItem): Result<Unit> {
        return Result.failure(IllegalStateException("Virtualization engine coming soon"))
    }
}
