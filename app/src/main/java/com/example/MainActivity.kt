package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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

    // Back button handling
    when (currentScreen) {
        Screen.SETTINGS -> BackHandler { viewModel.navigateTo(Screen.HOME) }
        Screen.BATTERY_HELPER -> BackHandler { viewModel.navigateTo(Screen.SETTINGS) }
        Screen.ONBOARDING -> BackHandler { /* Exit or stay on onboarding */ }
        Screen.HOME -> { /* Default system back */ }
    }

    // Modal Bottom Sheets & Dialogs
    if (isAddSheetVisible) {
        AddAppsBottomSheet(
            apps = installedApps,
            selectedPackages = selectedPackages,
            currentCloneCount = cloneCount,
            isVip = isVip,
            isLoading = isLoadingApps,
            searchQuery = searchQuery,
            onSearchChange = viewModel::setSearchQuery,
            onToggleSelect = viewModel::togglePackageSelection,
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissProfileSetup,
            title = { Text("Work Profile Required") },
            text = { Text("To create secure isolated app clones, Multiclonix Space requires an Android Work Profile container. Tap below to set it up.") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.dismissProfileSetup()
                        (context as? Activity)?.let {
                            com.example.admin.CloneAdminReceiver.startProvisioning(it)
                        }
                    }
                ) {
                    Text("Create Work Profile")
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
