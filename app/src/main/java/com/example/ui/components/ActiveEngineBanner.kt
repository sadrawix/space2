package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.EngineSelectionInfo
import com.example.engine.EngineType
import com.example.engine.WorkProfileEngine
import com.example.engine.WorkProfileState
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.GoldenAmber

@Composable
fun ActiveEngineBanner(
    engineInfo: EngineSelectionInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOem = engineInfo.activeEngine.engineType == EngineType.OEM_DUAL_APPS
    
    val wpState = if (!isOem) WorkProfileEngine.workProfileState(context) else WorkProfileState.READY

    val (badgeText, badgeColor) = when (wpState) {
        WorkProfileState.READY -> "Work Profile (Ready)" to EmeraldCloneGreen
        WorkProfileState.NEEDS_SETUP -> "Work Profile (Setup Required)" to GoldenAmber
        WorkProfileState.UNSUPPORTED -> "Work Profile (Unsupported)" to Color.Gray
    }

    val displayLabel = if (isOem) engineInfo.activeEngine.shortName else badgeText
    val accentColor = if (isOem) CyanNeon else badgeColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33101726))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("active_engine_chip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isOem) Icons.Default.Bolt else Icons.Default.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = displayLabel,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Engine details",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
