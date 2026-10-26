package com.example.engine

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.model.CloneItem

class OemDualAppEngine : CloneEngine {
    override val engineType: EngineType = EngineType.OEM_DUAL_APPS
    override val displayName: String = getOemEngineName()
    override val shortName: String = getOemFeatureLabel()
    override val description: String =
        "Direct hardware-assisted multi-account engine provided by your device manufacturer (${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }}). Zero battery overhead."
    override val isolationLevel: String = "OEM Native Dual App Space (${Build.BRAND.uppercase()})"

    companion object {
        fun isSupported(context: Context): Boolean {
            val mfg = Build.MANUFACTURER.lowercase()
            val brand = Build.BRAND.lowercase()
            return mfg.contains("samsung") ||
                   mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") ||
                   mfg.contains("huawei") || mfg.contains("honor") ||
                   mfg.contains("oppo") || mfg.contains("realme") ||
                   mfg.contains("oneplus") ||
                   mfg.contains("vivo") || mfg.contains("iqoo") ||
                   brand.contains("samsung") || brand.contains("xiaomi")
        }

        fun getOemFeatureLabel(): String {
            val mfg = Build.MANUFACTURER.lowercase()
            return when {
                mfg.contains("samsung") -> "Dual Messenger"
                mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") -> "Dual Apps"
                mfg.contains("huawei") || mfg.contains("honor") -> "Twin App"
                mfg.contains("oppo") || mfg.contains("realme") || mfg.contains("oneplus") -> "Clone Apps"
                mfg.contains("vivo") || mfg.contains("iqoo") -> "App Clone"
                else -> "Dual Space"
            }
        }

        fun getOemEngineName(): String {
            val mfg = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            return "$mfg Native Dual Space"
        }
    }

    fun isSupported(context: Context): Boolean = Companion.isSupported(context)

    override fun maxInstances(pkg: String): Int = 2

    override fun isTrulyIsolated(ctx: Context, pkg: String): Boolean = isSupported(ctx)

    override suspend fun listCloneableApps(context: Context): List<CloneableApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val selfPackage = context.packageName

        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPackage) return@mapNotNull null

            val appName = resolveInfo.loadLabel(pm).toString()
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = try {
                resolveInfo.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            val category = when {
                pkg.contains("whatsapp", ignoreCase = true) ||
                pkg.contains("telegram", ignoreCase = true) ||
                pkg.contains("messenger", ignoreCase = true) ||
                pkg.contains("viber", ignoreCase = true) ||
                pkg.contains("line", ignoreCase = true) -> "Messaging"

                pkg.contains("facebook", ignoreCase = true) ||
                pkg.contains("instagram", ignoreCase = true) ||
                pkg.contains("twitter", ignoreCase = true) ||
                pkg.contains("tiktok", ignoreCase = true) ||
                pkg.contains("snapchat", ignoreCase = true) ||
                pkg.contains("discord", ignoreCase = true) -> "Social"

                pkg.contains("game", ignoreCase = true) ||
                pkg.contains("play", ignoreCase = true) -> "Games"

                else -> "Utilities"
            }

            CloneableApp(
                packageName = pkg,
                appName = appName,
                category = category,
                isSystemApp = isSystem,
                icon = icon
            )
        }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    override suspend fun createClone(
        context: Context,
        packageName: String,
        appName: String
    ): Result<CloneItem> {
        return try {
            val cloneName = "$appName (OEM)"
            val clone = CloneItem(
                packageName = packageName,
                originalAppName = appName,
                displayName = cloneName,
                engineType = engineType.name,
                instanceIndex = 2,
                badgeLabel = "OEM",
                badgeColorHex = "#00E5FF"
            )
            Result.success(clone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun launchClone(context: Context, clone: CloneItem): Result<Unit> {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(clone.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                launchIntent.putExtra("is_dual_app", true)
                launchIntent.putExtra("clone_space_id", clone.instanceIndex)
                context.startActivity(launchIntent)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("No launch intent found for ${clone.packageName}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openOemDualAppSettings(context: Context): Boolean {
        val intents = getOemSettingsIntents()
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                // Continue
            }
        }
        return false
    }

    private fun getOemSettingsIntents(): List<Intent> {
        val mfg = Build.MANUFACTURER.lowercase()
        return when {
            mfg.contains("samsung") -> listOf(
                Intent().setComponent(ComponentName("com.samsung.android.da.daagent", "com.samsung.android.da.daagent.MainActivity")),
                Intent("com.samsung.android.action.DUAL_MESSENGER")
            )
            mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") -> listOf(
                Intent().setComponent(ComponentName("com.miui.securityadd", "com.miui.securityadd.dualapp.DualAppManageActivity")),
                Intent("miui.intent.action.OP_AUTO_START")
            )
            mfg.contains("huawei") || mfg.contains("honor") -> listOf(
                Intent().setComponent(ComponentName("com.huawei.android.twinapp", "com.huawei.android.twinapp.TwinAppSettingsActivity")),
                Intent("com.huawei.android.twinapp.SETTINGS")
            )
            mfg.contains("oppo") || mfg.contains("realme") -> listOf(
                Intent().setComponent(ComponentName("com.coloros.clone", "com.coloros.clone.CloneSettingsActivity")),
                Intent().setComponent(ComponentName("com.oppo.clone", "com.oppo.clone.CloneSettingsActivity"))
            )
            mfg.contains("oneplus") -> listOf(
                Intent().setComponent(ComponentName("com.oneplus.clone", "com.oneplus.clone.ParallelAppsActivity")),
                Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.app.applock.AppLockActivity"))
            )
            else -> emptyList()
        }
    }
}
