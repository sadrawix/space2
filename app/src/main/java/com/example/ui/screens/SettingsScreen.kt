package com.example.ui.screens

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.EngineSelectionInfo
import com.example.engine.OemDualAppEngine
import com.example.engine.WorkProfileEngine
import com.example.engine.WorkProfileState
import com.example.ui.components.AppLockDialog
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SpaceCardGlass
import com.example.ui.theme.SpaceDarkBackground
import com.example.ui.theme.TextGraySecondary
import com.example.ui.theme.TextWhitePrimary

data class EngineOption(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val hint: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    engineInfo: EngineSelectionInfo,
    preferredEngine: String,
    isSpaceLockEnabled: Boolean,
    isVip: Boolean,
    currentLanguage: String,
    onPreferredEngineChange: (String) -> Unit,
    onSpaceLockChange: (Boolean, String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onOpenBatteryHelper: () -> Unit,
    onOpenVipStore: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    val wpState = remember { WorkProfileEngine.workProfileState(context) }
    val isOemSupported = remember { OemDualAppEngine().isSupported(context) }
    val oemLabel = if (isOemSupported) OemDualAppEngine.getOemFeatureLabel() else null

    val engineOptions = remember(wpState, isOemSupported, oemLabel) {
        listOf(
            EngineOption(
                id = "AUTO",
                label = "Auto Select (Recommended)",
                enabled = true,
                hint = "Automatically selects the best available engine for your device"
            ),
            EngineOption(
                id = "OEM_DUAL_APP",
                label = "OEM Native (${oemLabel ?: "Not available"})",
                enabled = isOemSupported,
                hint = if (isOemSupported) "Manufacturer-assisted zero-overhead space" else "Not supported on this phone model"
            ),
            EngineOption(
                id = "WORK_PROFILE",
                label = "Work Profile Sandbox",
                enabled = wpState != WorkProfileState.UNSUPPORTED,
                hint = when (wpState) {
                    WorkProfileState.READY -> "Ready and fully active"
                    WorkProfileState.NEEDS_SETUP -> "Setup required (Work Profile not provisioned)"
                    WorkProfileState.UNSUPPORTED -> "Not supported by device OS"
                }
            ),
            EngineOption(
                id = "VIRTUALIZATION",
                label = "Virtualization Engine",
                enabled = false,
                hint = "Coming soon (Experimental sandbox)"
            )
        )
    }

    if (showSetPinDialog) {
        AppLockDialog(
            title = "Set Space Lock PIN",
            subtitle = "Enter 4 digits to secure your Multiclonix space",
            onPinSubmit = { pin ->
                if (pin.length == 4) {
                    onSpaceLockChange(true, pin)
                    showSetPinDialog = false
                    true
                } else false
            },
            onDismiss = { showSetPinDialog = false },
            isCancelable = true
        )
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        color = TextWhitePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhitePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceDarkBackground)
            )
        },
        containerColor = SpaceDarkBackground,
        modifier = modifier.fillMaxSize().navigationBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // VIP / Subscription Status Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenVipStore)
                        .border(
                            1.dp,
                            if (isVip) GoldenAmber else Color(0x334DD0E1),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("settings_vip_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isVip) GoldenAmber else CoralRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVip) "Multiclonix VIP (Active)" else "Multiclonix Free Tier",
                                color = TextWhitePrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isVip) "Unlimited clones & zero restrictions" else "Max 3 active clones. Tap to upgrade to VIP.",
                                color = TextGraySecondary,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextGraySecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Clone Engine Strategy Selection with true state and hints
            item {
                Text(
                    text = "CLONE ENGINE STRATEGY",
                    color = TextGraySecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val statusText = when (wpState) {
                            WorkProfileState.READY -> "Work Profile: Ready (OS-level isolated profile)"
                            WorkProfileState.NEEDS_SETUP -> "Work Profile: Setup Required (Not yet provisioned)"
                            WorkProfileState.UNSUPPORTED -> "Work Profile: Unsupported on this device"
                        }
                        Text(
                            text = statusText,
                            color = when (wpState) {
                                WorkProfileState.READY -> EmeraldCloneGreen
                                WorkProfileState.NEEDS_SETUP -> GoldenAmber
                                WorkProfileState.UNSUPPORTED -> Color.Gray
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (wpState == WorkProfileState.NEEDS_SETUP) {
                            Button(
                                onClick = {
                                    WorkProfileEngine.startProvisioning(context as Activity)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("provision_work_profile_button")
                            ) {
                                Text("Create / Setup Work Profile Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        engineOptions.forEach { option ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = option.enabled) {
                                        onPreferredEngineChange(option.id)
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = preferredEngine == option.id,
                                        onClick = if (option.enabled) { { onPreferredEngineChange(option.id) } } else null,
                                        enabled = option.enabled,
                                        colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option.label,
                                        color = if (option.enabled) TextWhitePrimary else TextGraySecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (option.hint != null) {
                                    Text(
                                        text = option.hint,
                                        color = TextGraySecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 40.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Security & Space Lock
            item {
                Text(
                    text = "PRIVACY & SECURITY",
                    color = TextGraySecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isSpaceLockEnabled) CoralRed else TextGraySecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Space PIN Lock (SHA-256 Hashed)",
                                        color = TextWhitePrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isSpaceLockEnabled) "PIN code secured" else "Protect app entry with secure PIN",
                                        color = TextGraySecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isSpaceLockEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        showSetPinDialog = true
                                    } else {
                                        onSpaceLockChange(false, "")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CoralRed
                                ),
                                modifier = Modifier.testTag("settings_space_lock_switch")
                            )
                        }
                    }
                }
            }

            // Optimization & Helper
            item {
                Text(
                    text = "OPTIMIZATION & HELPER",
                    color = TextGraySecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenBatteryHelper)
                        .testTag("settings_battery_helper_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = GoldenAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.battery_helper),
                                color = TextWhitePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Fix delayed notifications and background kills",
                                color = TextGraySecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextGraySecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Language & RTL
            item {
                Text(
                    text = "LANGUAGE & LOCALIZATION",
                    color = TextGraySecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "App Language / اللغة",
                                color = TextWhitePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val languages = listOf("en" to "English", "ar" to "العربية (Arabic RTL)")
                        languages.forEach { (code, name) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLanguageChange(code) }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = currentLanguage == code,
                                    onClick = { onLanguageChange(code) },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    color = TextWhitePrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Privacy Policy & About
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyPolicy = true }
                        .testTag("settings_privacy_policy_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = EmeraldCloneGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.privacy_policy),
                                color = TextWhitePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "100% on-device data safety guarantee",
                                color = TextGraySecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextGraySecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Multiclonix Space v1.0.0 • Clean Architecture",
                    color = TextGraySecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
