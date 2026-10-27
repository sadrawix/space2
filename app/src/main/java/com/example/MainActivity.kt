package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AppLockDialog
import com.example.ui.screens.AddAppsBottomSheet
import com.example.ui.screens.BatteryHelperScreen
import com.example.ui.screens.CloneDetailBottomSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VipStoreDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceDarkBackground

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val layoutDirection = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = SpaceDarkBackground
                    ) {
                        MulticlonixApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MulticlonixApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allClones by viewModel.allClones.collectAsStateWithLifecycle()
    val cloneCount by viewModel.cloneCount.collectAsStateWithLifecycle()
    val isVip by viewModel.isVip.collectAsStateWithLifecycle()
    val isSpaceLockEnabled by viewModel.isSpaceLockEnabled.collectAsStateWithLifecycle()
    val isSpaceUnlocked by viewModel.isSpaceUnlocked.collectAsStateWithLifecycle()
    val engineInfo by viewModel.engineInfo.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val isAddSheetVisible by viewModel.isAddSheetVisible.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val selectedPackages by viewModel.selectedPackages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()

    val selectedCloneForDetail by viewModel.selectedCloneForDetail.collectAsStateWithLifecycle()
    val selectedCloneForPin by viewModel.selectedCloneForPin.collectAsStateWithLifecycle()
    val isVipStoreVisible by viewModel.isVipStoreVisible.collectAsStateWithLifecycle()
    val preferredEngine by viewModel.preferredEngine.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    val isOemSupported = remember(context) { com.example.engine.OemDualAppEngine.isSupported(context) }
    var provisioningFailed by remember { mutableStateOf(false) }

    val provisionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.refreshEngine()
        if (result.resultCode != Activity.RESULT_OK) {
            val state = com.example.engine.WorkProfiles.state(context)
            if (state != com.example.engine.WorkProfileState.READY) {
                provisioningFailed = true
            }
        }
    }

    // Back button handling
    when (currentScreen) {
        Screen.SETTINGS -> BackHandler { viewModel.navigateTo(Screen.HOME) }
        Screen.BATTERY_HELPER -> BackHandler { viewModel.navigateTo(Screen.SETTINGS) }
        Screen.ONBOARDING -> BackHandler { /* Exit or stay on onboarding */ }
        Screen.HOME -> { /* Default system back */ }
    }

    // Modal Bottom Sheets & Dialogs
    if (isAddSheetVisible) {
        com.example.ui.screens.AppPickerBottomSheet(
            apps = installedApps,
            selectedPackages = selectedPackages,
            currentCloneCount = cloneCount,
            isVip = isVip,
            isLoading = isLoadingApps,
            searchQuery = searchQuery,
            onSearchChange = viewModel::setSearchQuery,
            onToggleSelect = viewModel::togglePackageSelection,
            onAppClick = { pkg, name -> viewModel.createIsolatedInstance(pkg, name) },
            onCancel = viewModel::hideAddSheet,
            onConfirmClone = viewModel::createClonesForSelected
        )
    }

    selectedCloneForDetail?.let { clone ->
        CloneDetailBottomSheet(
            clone = clone,
            onDismiss = { viewModel.showCloneDetail(null) },
            onSave = viewModel::updateClone,
            onDelete = viewModel::deleteClone,
            onLaunch = { c ->
                viewModel.showCloneDetail(null)
                viewModel.launchClone(context, c)
            }
        )
    }

    selectedCloneForPin?.let { clone ->
        AppLockDialog(
            title = "Locked Clone",
            subtitle = "Enter PIN to launch ${clone.displayName}",
            onPinSubmit = { pin ->
                viewModel.unlockAndLaunchClone(context, clone, pin)
            },
            onDismiss = { viewModel.showClonePinPrompt(null) },
            isCancelable = true
        )
    }

    if (isVipStoreVisible) {
        VipStoreDialog(
            isVip = isVip,
            onUpgrade = viewModel::setVip,
            onDismiss = { viewModel.showVipStore(false) }
        )
    }

    val needsProfileSetup by viewModel.needsProfileSetup.collectAsStateWithLifecycle()
    if (needsProfileSetup) {
        val check = remember(context) { com.example.admin.checkProvisioning(context) }
        val reason = when (check) {
            is com.example.admin.ProvisionCheck.Allowed -> "To create fully isolated clones, Android requires a Work Profile container."
            is com.example.admin.ProvisionCheck.Blocked -> check.reason
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissProfileSetup,
            title = { Text("Work Profile Setup") },
            text = {
                androidx.compose.foundation.layout.Column {
                    Text(reason)
                    if (isOemSupported) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Recommended Alternative: Your device supports native OEM Dual Space cloning without Work Profile restrictions.",
                            color = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    if (isOemSupported) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                viewModel.dismissProfileSetup()
                                viewModel.setPreferredEngine("OEM_DUAL_APPS")
                            }
                        ) {
                            Text("Use OEM Engine")
                        }
                    }
                    if (check is com.example.admin.ProvisionCheck.Allowed) {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.dismissProfileSetup()
                                try {
                                    val intent = com.example.admin.CloneAdminReceiver.getProvisioningIntent(context)
                                    provisionLauncher.launch(intent)
                                } catch (e: Exception) {
                                    (context as? Activity)?.let {
                                        com.example.admin.CloneAdminReceiver.startProvisioning(it)
                                    }
                                }
                            }
                        ) {
                            Text("Setup Profile")
                        }
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = viewModel::dismissProfileSetup
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (provisioningFailed) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { provisioningFailed = false },
            title = { Text("Work Profile Rejected by Device") },
            text = {
                androidx.compose.foundation.layout.Column {
                    Text("Android system rejected setup ('Can't set up device'). This occurs when:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• A work profile, Secure Folder, or secondary user already exists on this phone.")
                    Text("• An enterprise MDM or device policy restricts creating managed profiles.")
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isOemSupported) {
                        Text(
                            "Recommended: Switch to native OEM Dual Apps for zero-overhead app cloning.",
                            color = androidx.compose.ui.graphics.Color(0xFF00E5FF)
                        )
                    } else {
                        Text(
                            "You can inspect existing work profiles in Phone Settings > Accounts.",
                            color = androidx.compose.ui.graphics.Color(0xFFFFA000)
                        )
                    }
                }
            },
            confirmButton = {
                if (isOemSupported) {
                    androidx.compose.material3.Button(
                        onClick = {
                            provisioningFailed = false
                            viewModel.setPreferredEngine("OEM_DUAL_APPS")
                        }
                    ) {
                        Text("Switch to OEM Engine")
                    }
                } else {
                    androidx.compose.material3.Button(
                        onClick = {
                            provisioningFailed = false
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_SYNC_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    ) {
                        Text("Open Accounts Settings")
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { provisioningFailed = false }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Main Screens
    when (currentScreen) {
        Screen.ONBOARDING -> {
            OnboardingScreen(
                onGetStarted = viewModel::completeOnboarding
            )
        }
        Screen.HOME -> {
            HomeScreen(
                clones = allClones,
                isVip = isVip,
                isSpaceLockEnabled = isSpaceLockEnabled,
                isSpaceUnlocked = isSpaceUnlocked,
                engineInfo = engineInfo,
                toastMessage = toastMessage,
                onAddClick = viewModel::showAddSheet,
                onCloneClick = { clone -> viewModel.launchClone(context, clone) },
                onCloneLongClick = viewModel::showCloneDetail,
                onVipClick = { viewModel.showVipStore(true) },
                onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) },
                onLockSpace = {
                    if (isSpaceLockEnabled) {
                        viewModel.lockSpace()
                    } else {
                        viewModel.navigateTo(Screen.SETTINGS)
                    }
                },
                onUnlockSpace = viewModel::unlockSpace,
                onClearToast = viewModel::clearToast
            )
        }
        Screen.SETTINGS -> {
            SettingsScreen(
                engineInfo = engineInfo,
                preferredEngine = preferredEngine,
                isSpaceLockEnabled = isSpaceLockEnabled,
                isVip = isVip,
                currentLanguage = language,
                onPreferredEngineChange = viewModel::setPreferredEngine,
                onSpaceLockChange = viewModel::setSpaceLock,
                onLanguageChange = viewModel::setLanguage,
                onOpenBatteryHelper = { viewModel.navigateTo(Screen.BATTERY_HELPER) },
                onOpenVipStore = { viewModel.showVipStore(true) },
                onBack = { viewModel.navigateTo(Screen.HOME) }
            )
        }
        Screen.BATTERY_HELPER -> {
            BatteryHelperScreen(
                onBack = { viewModel.navigateTo(Screen.SETTINGS) }
            )
        }
    }
}
