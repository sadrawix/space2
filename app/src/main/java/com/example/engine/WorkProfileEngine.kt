package com.example.engine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import com.example.admin.CloneAdminReceiver
import com.example.data.model.CloneItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WorkProfileEngine : CloneEngine {

    override val engineType = EngineType.WORK_PROFILE
    override val displayName = "Android Work Profile Sandbox"
    override val shortName = "Work Profile"
    override val description = "OS-level isolated work profile with secure separation of data and apps."
    override val isolationLevel = "System-level isolated profile (Work Profile)"

    companion object {
        fun workProfileState(ctx: Context): WorkProfileState = WorkProfiles.state(ctx)
        fun startProvisioning(activity: Activity) = CloneAdminReceiver.startProvisioning(activity)
    }

    // One managed profile = one copy per app.
    override fun maxInstances(pkg: String) = 1

    override fun isTrulyIsolated(ctx: Context, pkg: String): Boolean {
        return try {
            val handle = WorkProfiles.handle(ctx) ?: return false
            val la = ctx.getSystemService(LauncherApps::class.java) ?: return false
            la.getActivityList(pkg, handle).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun listCloneableApps(ctx: Context): List<CloneableApp> =
        withContext(Dispatchers.IO) {
            try {
                val pm = ctx.packageManager ?: return@withContext emptyList()
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                    .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
                    .distinctBy { it.first }
                    .filter { it.first != ctx.packageName }
                    .sortedBy { it.second.lowercase() }
                    .map { (pkg, label) -> CloneableApp(pkg, label, "Other") }
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun createClone(
        ctx: Context, pkg: String, appName: String
    ): Result<CloneItem> = runCatching {
        val handle = WorkProfiles.handle(ctx)
            ?: error("Work Profile is not set up")
        val la = ctx.getSystemService(LauncherApps::class.java)
            ?: error("LauncherApps service unavailable")

        if (la.getActivityList(pkg, handle).isEmpty()) {
            // Ask OUR copy inside the profile (Profile Owner) to install it.
            ctx.startActivity(
                Intent(CloneAdminReceiver.ACTION_INSTALL_PKG)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra("pkg", pkg)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            var installed = false
            repeat(10) {
                if (!installed) {
                    delay(500)
                    installed = la.getActivityList(pkg, handle).isNotEmpty()
                }
            }
            if (!installed) error("Could not install the app inside the Work Profile")
        }

        CloneItem(
            packageName = pkg,
            originalAppName = appName,
            displayName = "$appName 2",
            engineType = engineType.name,
            instanceIndex = 2
        )
    }

    override suspend fun launchClone(ctx: Context, clone: CloneItem): Result<Unit> =
        runCatching {
            val handle = WorkProfiles.handle(ctx)
                ?: error("Work Profile is not set up")
            val la = ctx.getSystemService(LauncherApps::class.java)
                ?: error("LauncherApps service unavailable")
            val activity = la.getActivityList(clone.packageName, handle).firstOrNull()
                ?: error("App is not installed inside the Work Profile")
            la.startMainActivity(activity.componentName, handle, null, null)
        }
}
