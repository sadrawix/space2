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
import android.widget.Toast

class CloneAdminReceiver : DeviceAdminReceiver() {

    companion object {
        const val ACTION_INSTALL_PKG = "com.example.INSTALL_PKG"
        const val REQUEST_PROVISION = 1001

        fun component(ctx: Context) = ComponentName(ctx, CloneAdminReceiver::class.java)

        /** Call from an Activity when the user taps "Create Work Profile". */
        fun startProvisioning(activity: Activity) {
            val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
                .putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    component(activity)
                )
                .putExtra(
                    @Suppress("DEPRECATION")
                    DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    activity.packageName
                )
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(intent, REQUEST_PROVISION)
            } else {
                Toast.makeText(
                    activity, "Work Profile is not supported on this phone", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Runs inside the new profile.
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = component(context)
        dpm.setProfileName(admin, "Multiclonix Space")

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
        dpm.addCrossProfileIntentFilter(
            admin, filter, DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
        )

        dpm.setProfileEnabled(admin)
    }
}

/** Runs inside the work profile and installs the requested package there. */
class InstallInProfileActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("pkg")
        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (pkg != null && dpm.isProfileOwnerApp(packageName)) {
            dpm.installExistingPackage(CloneAdminReceiver.component(this), pkg)
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
