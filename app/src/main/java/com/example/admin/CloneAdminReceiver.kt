package com.example.admin

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import com.example.engine.WorkProfiles

sealed interface ProvisionCheck {
    object Allowed : ProvisionCheck
    data class Blocked(val reason: String) : ProvisionCheck
}

fun checkProvisioning(ctx: Context): ProvisionCheck {
    val dpm = ctx.getSystemService(DevicePolicyManager::class.java)
    val um = ctx.getSystemService(UserManager::class.java)

    if (ctx.packageManager?.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS) != true)
        return ProvisionCheck.Blocked("This phone does not support Work Profile.")
    if (WorkProfiles.hasAnyProfile(ctx))
        return ProvisionCheck.Blocked("A work profile or managed space already exists on this phone (e.g. Secure Folder, Shelter, or corporate account). Android only allows one.")
    if (um != null && um.hasUserRestriction(UserManager.DISALLOW_ADD_MANAGED_PROFILE))
        return ProvisionCheck.Blocked("Work profiles are blocked by a device policy.")
    if (dpm == null || !dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE))
        return ProvisionCheck.Blocked("Android refused to create a work profile on this device (DevicePolicy block).")
    return ProvisionCheck.Allowed
}

class CloneAdminReceiver : DeviceAdminReceiver() {

    companion object {
        const val ACTION_INSTALL_PKG = "com.example.INSTALL_PKG"
        const val REQUEST_PROVISION = 1001

        fun component(ctx: Context) = ComponentName(ctx, CloneAdminReceiver::class.java)

        fun getProvisioningIntent(ctx: Context): Intent {
            return Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    component(ctx)
                )
                putExtra(
                    @Suppress("DEPRECATION")
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    ctx.packageName
                )
            }
        }

        /** Call from an Activity when the user taps "Create Work Profile". */
        fun startProvisioning(activity: Activity): String? {
            when (val check = checkProvisioning(activity)) {
                is ProvisionCheck.Allowed -> {
                    val intent = getProvisioningIntent(activity)
                    if (intent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivityForResult(intent, REQUEST_PROVISION)
                        return null
                    } else {
                        val reason = "Work Profile is not supported on this phone"
                        Toast.makeText(activity, reason, Toast.LENGTH_LONG).show()
                        return reason
                    }
                }
                is ProvisionCheck.Blocked -> {
                    Toast.makeText(activity, check.reason, Toast.LENGTH_LONG).show()
                    return check.reason
                }
            }
        }
    }

    // Runs inside the new profile.
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = component(context)
        try {
            dpm?.setProfileName(admin, "Multiclonix Space")

            // The install activity is disabled in the manifest so it does NOT resolve
            // in the personal profile. Enable it here, inside the work profile only.
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, InstallInProfileActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Personal -> work: forward this intent into the managed profile,
            // because no activity resolves it in the personal profile.
            val filter = IntentFilter(ACTION_INSTALL_PKG).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm?.addCrossProfileIntentFilter(
                admin,
                filter,
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
            )

            dpm?.setProfileEnabled(admin)
        } catch (e: Exception) {
            // Fallback
        }
    }
}

/** Runs inside the work profile and installs the requested package there. */
class InstallInProfileActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("pkg")
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, CloneAdminReceiver::class.java)
        if (pkg != null && dpm != null && dpm.isProfileOwnerApp(packageName)) {
            try {
                dpm.installExistingPackage(admin, pkg)
            } catch (e: Exception) {
                // Ignore or log
            }
        }
        finish()
    }
}

/** Required by the provisioning flow on Android 9+. */
class ProvisioningModeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK, Intent().putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
            DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE
        ))
        finish()
    }
}

class PolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK)
        finish()
    }
}
