package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.TextWhitePrimary

@Composable
fun TopBar(
    isVip: Boolean,
    isSpaceLockEnabled: Boolean,
    onVipClick: () -> Unit,
    onLockToggleClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Red Shopping Basket VIP Icon (matches DualSpace screenshot)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onVipClick)
                .testTag("vip_store_button"),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = if (isVip) GoldenAmber else CoralRed,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (isVip) "VIP" else "PRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = stringResource(R.string.store_vip),
                    tint = CoralRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Center Title: "Multiclonix Space"
        Text(
            text = stringResource(R.string.app_name),
            color = TextWhitePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.testTag("app_title_text")
        )

        // Right Icons: Lock & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onLockToggleClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("space_lock_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSpaceLockEnabled) Icons.Default.Lock else Icons.Default.Security,
                    contentDescription = stringResource(R.string.privacy_lock),
                    tint = if (isSpaceLockEnabled) CoralRed else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
