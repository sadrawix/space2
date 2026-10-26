package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CloneItem
import com.example.engine.EngineSelectionInfo
import com.example.ui.components.ActiveEngineBanner
import com.example.ui.components.AddCloneTile
import com.example.ui.components.AppLockDialog
import com.example.ui.components.CloneGridTile
import com.example.ui.components.TopBar
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.SpaceDarkBackground
import com.example.ui.theme.TextWhitePrimary
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    clones: List<CloneItem>,
    isVip: Boolean,
    isSpaceLockEnabled: Boolean,
    isSpaceUnlocked: Boolean,
    engineInfo: EngineSelectionInfo,
    toastMessage: String?,
    onAddClick: () -> Unit,
    onCloneClick: (CloneItem) -> Unit,
    onCloneLongClick: (CloneItem) -> Unit,
    onVipClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLockSpace: () -> Unit,
    onUnlockSpace: (String) -> Boolean,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEngineInfoDialog by remember { mutableStateOf(false) }

    // Space Lock dialog when space is locked
    if (isSpaceLockEnabled && !isSpaceUnlocked) {
        AppLockDialog(
            title = "Space Locked",
            subtitle = "Enter PIN to access your private Multiclonix space",
            onPinSubmit = onUnlockSpace,
            onDismiss = {},
            isCancelable = false
        )
    }

    if (showEngineInfoDialog) {
        EngineInfoDialog(
            engineInfo = engineInfo,
            onDismiss = { showEngineInfoDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBackground)
    ) {
        // Dark blurred cosmic nebula wallpaper background matching Screenshot 1 & 2
        Image(
            painter = painterResource(id = R.drawable.space_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC080C14),
                            Color(0x80080C14),
                            Color(0xDD080C14)
                        )
                    )
                )
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: VIP Store basket, Centered App Title, Lock & Settings icons
            TopBar(
                isVip = isVip,
                isSpaceLockEnabled = isSpaceLockEnabled,
                onVipClick = onVipClick,
                onLockToggleClick = onLockSpace,
                onSettingsClick = onSettingsClick
            )

            // Active Engine Chip
            ActiveEngineBanner(
                engineInfo = engineInfo,
                onClick = { showEngineInfoDialog = true },
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Clones Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("clones_grid")
            ) {
                // Cloned App Tiles
                items(clones, key = { it.id }) { clone ->
                    CloneGridTile(
                        clone = clone,
                        onClick = { onCloneClick(clone) },
                        onLongClick = { onCloneLongClick(clone) }
                    )
                }

                // Frosted "+" Tile to add clones (matches Screenshot 1 & 2)
                item {
                    AddCloneTile(
                        onClick = onAddClick
                    )
                }
            }
        }

        // Floating Toast Message
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (toastMessage != null) {
                LaunchedEffect(toastMessage) {
                    delay(3000)
                    onClearToast()
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xE61E283E))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = toastMessage,
                        color = TextWhitePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EngineInfoDialog(
    engineInfo: EngineSelectionInfo,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = SpaceDarkBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Dual Space Engine",
                    color = TextWhitePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Active: ${engineInfo.activeEngine.displayName}",
                    color = EmeraldCloneGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = engineInfo.activeEngine.description,
                    color = TextWhitePrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Security Level: ${engineInfo.activeEngine.isolationLevel}",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = EmeraldCloneGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
