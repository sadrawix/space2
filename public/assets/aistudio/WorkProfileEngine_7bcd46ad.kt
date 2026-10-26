package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import com.example.admin.CloneAdminReceiver
import com.example.data.model.CloneItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WorkProfileEngine : CloneEngine {

    override val engineType = EngineType.WORK_PROFILE

    // One managed profile = one copy per app.
    override fun maxInstances(pkg: String) = 1

    override suspend fun listCloneableApps(ctx: Context): List<CloneableApp> =
        withContext(Dispatchers.IO) {
            val pm = ctx.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
                .distinctBy { it.first }
                .filter { it.first != ctx.packageName }
                .sortedBy { it.second.lowercase() }
                .map { (pkg, label) -> CloneableApp(pkg, label, "Other") }
        }

    override suspend fun createClone(
        ctx: Context, pkg: String, appName: String
    ): Result<CloneItem> = runCatching {
        val handle = WorkProfiles.handle(ctx)
            ?: error("Work Profile is not set up")
        val la = ctx.getSystemService(LauncherApps::class.java)

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
            val activity = la.getActivityList(clone.packageName, handle).firstOrNull()
                ?: error("App is not installed inside the Work Profile")
            la.startMainActivity(activity.componentName, handle, null, null)
        }
}
