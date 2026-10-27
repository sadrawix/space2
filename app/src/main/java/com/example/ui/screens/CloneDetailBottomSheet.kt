package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CloneItem
import com.example.ui.theme.BadgeColors
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.SpaceCardGlass
import com.example.ui.theme.SpaceDarkBackground
import com.example.ui.theme.TextGraySecondary
import com.example.ui.theme.TextWhitePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneDetailBottomSheet(
    clone: CloneItem,
    onDismiss: () -> Unit,
    onSave: (CloneItem) -> Unit,
    onDelete: (Long) -> Unit,
    onLaunch: (CloneItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var displayName by remember(clone) { mutableStateOf(clone.displayName) }
    var badgeLabel by remember(clone) { mutableStateOf(clone.badgeLabel) }
    var selectedBadgeColor by remember(clone) { mutableStateOf(clone.badgeColorHex) }
    var isLocked by remember(clone) { mutableStateOf(clone.isLocked) }
    var notificationsEnabled by remember(clone) { mutableStateOf(clone.notificationsEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpaceDarkBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("clone_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.clone_options),
                    color = TextWhitePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Quick Launch Button
                Button(
                    onClick = { onLaunch(clone) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCloneGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp).testTag("quick_launch_clone_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rename Field
            Text(
                text = "Display Name",
                color = TextGraySecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SpaceCardGlass,
                    unfocusedContainerColor = SpaceCardGlass,
                    focusedTextColor = TextWhitePrimary,
                    unfocusedTextColor = TextWhitePrimary,
                    focusedIndicatorColor = CyanNeon,
                    unfocusedIndicatorColor = Color(0x334DD0E1)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("clone_rename_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Badge Label Field
            Text(
                text = "Badge Text (e.g. DUAL, WORK, 2ND)",
                color = TextGraySecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = badgeLabel,
                onValueChange = { if (it.length <= 6) badgeLabel = it.uppercase() },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SpaceCardGlass,
                    unfocusedContainerColor = SpaceCardGlass,
                    focusedTextColor = TextWhitePrimary,
                    unfocusedTextColor = TextWhitePrimary,
                    focusedIndicatorColor = CyanNeon,
                    unfocusedIndicatorColor = Color(0x334DD0E1)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("clone_badge_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Badge Color Palette
            Text(
                text = "Badge Tint Color",
                color = TextGraySecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgeColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedBadgeColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedBadgeColor = hex }
                    )
                }
            }

            if (clone.sandboxPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1A00E5FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ISOLATED SANDBOX ENVIRONMENT",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UUID: ${clone.sandboxUuid.ifEmpty { "N/A" }}",
                            color = TextWhitePrimary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Path: ${clone.sandboxPath}",
                            color = TextGraySecondary,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Isolated storage: shared_prefs/ • databases/ • files/",
                            color = EmeraldCloneGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Switches: Lock & Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isLocked) CoralRed else TextGraySecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.lock_clone),
                            color = TextWhitePrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Require Space PIN to launch",
                            color = TextGraySecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isLocked,
                    onCheckedChange = { isLocked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CoralRed
                    ),
                    modifier = Modifier.testTag("lock_clone_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (notificationsEnabled) CyanNeon else TextGraySecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.clone_notifications),
                            color = TextWhitePrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Allow notifications in cloned instance",
                            color = TextGraySecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanNeon
                    ),
                    modifier = Modifier.testTag("notifications_clone_switch")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Delete and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onDelete(clone.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CoralRed.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("delete_clone_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.delete_clone), fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        onSave(
                            clone.copy(
                                displayName = displayName.ifBlank { clone.originalAppName },
                                badgeLabel = badgeLabel.ifBlank { "CLONE" },
                                badgeColorHex = selectedBadgeColor,
                                isLocked = isLocked,
                                notificationsEnabled = notificationsEnabled
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = SpaceDarkBackground),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_clone_button")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
