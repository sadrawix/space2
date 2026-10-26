package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SpaceCardGlass
import com.example.ui.theme.SpaceDarkBackground
import com.example.ui.theme.TextGraySecondary
import com.example.ui.theme.TextWhitePrimary

data class BrandGuide(
    val brandId: String,
    val brandName: String,
    val steps: List<String>,
    val tips: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHelperScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMfg = Build.MANUFACTURER.lowercase()

    val guides = remember {
        listOf(
            BrandGuide(
                brandId = "samsung",
                brandName = "Samsung (One UI)",
                steps = listOf(
                    "Open Device Settings → Apps → Multiclonix Space.",
                    "Tap on 'Battery'.",
                    "Choose 'Unrestricted' instead of Optimized.",
                    "Go to Settings → Battery and Device Care → Battery → Background usage limits → Add Multiclonix to 'Never sleeping apps'."
                ),
                tips = "Samsung actively puts cloned background processes to sleep after 3 days of low activity unless added to Never Sleeping Apps."
            ),
            BrandGuide(
                brandId = "xiaomi",
                brandName = "Xiaomi / POCO (MIUI / HyperOS)",
                steps = listOf(
                    "Open Settings → Apps → Manage apps → Multiclonix Space.",
                    "Enable the 'Autostart' toggle switch.",
                    "Scroll down to 'Battery Saver' and select 'No restrictions'.",
                    "Open the Recent Apps screen, long press Multiclonix Space, and tap the Padlock icon to lock it in RAM."
                ),
                tips = "Xiaomi's MIUI kills background sockets aggressively. Enabling Autostart + No Restrictions guarantees instant WhatsApp/Telegram clone messages."
            ),
            BrandGuide(
                brandId = "huawei",
                brandName = "Huawei / Honor (EMUI)",
                steps = listOf(
                    "Open Settings → Battery → App Launch.",
                    "Find Multiclonix Space and toggle it from 'Manage automatically' to 'Manage manually'.",
                    "Ensure all three switches ('Auto-launch', 'Secondary launch', 'Run in background') are ON."
                ),
                tips = "EMUI terminates secondary tasks on screen-off unless manual launch management is granted."
            ),
            BrandGuide(
                brandId = "oppo",
                brandName = "Oppo / Realme (ColorOS)",
                steps = listOf(
                    "Open Settings → App Management → Multiclonix Space.",
                    "Tap 'Battery usage' and enable 'Allow background activity'.",
                    "Enable 'Allow auto-startup' under app permissions.",
                    "In Recent Apps, swipe down on the app card and tap the lock icon."
                ),
                tips = "ColorOS automatically freezes background clones after 10 minutes of screen timeout without auto-startup permission."
            ),
            BrandGuide(
                brandId = "oneplus",
                brandName = "OnePlus (OxygenOS)",
                steps = listOf(
                    "Go to Settings → Battery → Battery Optimization / Advanced settings.",
                    "Find Multiclonix Space and select 'Don't optimize'.",
                    "Under App info → Battery, enable 'Allow background activity'."
                ),
                tips = "OxygenOS's Deep Optimization aggressively kills push notification listeners unless set to 'Don't optimize'."
            ),
            BrandGuide(
                brandId = "vivo",
                brandName = "Vivo / iQOO (FuntouchOS)",
                steps = listOf(
                    "Open Settings → Battery → High background power consumption.",
                    "Find Multiclonix Space and turn the switch ON.",
                    "Go to Settings → Applications and Permissions → Autostart → Enable Multiclonix Space."
                ),
                tips = "FuntouchOS strictly prohibits background socket listening unless granted High background power consumption."
            ),
            BrandGuide(
                brandId = "stock",
                brandName = "Google Pixel / Motorola / Stock",
                steps = listOf(
                    "Open Settings → Apps → Multiclonix Space.",
                    "Tap 'App battery usage'.",
                    "Select 'Unrestricted'.",
                    "Ensure 'Pause app activity if unused' is switched OFF."
                ),
                tips = "Pure Android uses Adaptive Battery. Setting Unrestricted prevents Android from deferring sync jobs."
            )
        )
    }

    // Preselect based on device manufacturer
    var selectedBrandId by remember {
        val matched = guides.firstOrNull { guide -> currentMfg.contains(guide.brandId) }
        mutableStateOf(matched?.brandId ?: "stock")
    }

    val activeGuide = guides.first { it.brandId == selectedBrandId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.battery_helper),
                        color = TextWhitePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("battery_helper_back_button")) {
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
            // Warning Callout Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GoldenAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = GoldenAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Why are clone notifications delayed?",
                                color = GoldenAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Most phone manufacturers aggressively kill background apps and clones to boast artificial battery life numbers. Follow these steps to ensure your cloned accounts receive messages instantly.",
                                color = TextWhitePrimary.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Manufacturer Selector Pills
            item {
                Text(
                    text = "Select Phone Manufacturer (Detected: ${Build.MANUFACTURER.uppercase()})",
                    color = TextGraySecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(guides) { guide ->
                        val isSelected = guide.brandId == selectedBrandId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanNeon else SpaceCardGlass)
                                .clickable { selectedBrandId = guide.brandId }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("brand_tab_${guide.brandId}")
                        ) {
                            Text(
                                text = guide.brandName.substringBefore(" "),
                                color = if (isSelected) SpaceDarkBackground else TextWhitePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Step-by-Step Instructions Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = EmeraldCloneGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeGuide.brandName,
                                color = TextWhitePrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        activeGuide.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(CyanNeon.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = CyanNeon,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = step,
                                    color = TextWhitePrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tips
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1A4DD0E1))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Note: ${activeGuide.tips}",
                                color = CyanNeon,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Direct Action Buttons
            item {
                Button(
                    onClick = {
                        openBatterySettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCloneGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_system_battery_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Battery Optimization Settings",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

fun openBatterySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val appIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
        } catch (e2: Exception) {
            // Fallback to standard settings
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
        }
    }
}
