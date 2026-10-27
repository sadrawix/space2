package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.CloneableApp
import com.example.ui.components.getAppIconBitmap
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldCloneGreen
import com.example.ui.theme.SpaceCardGlass
import com.example.ui.theme.SpaceDarkBackground
import com.example.ui.theme.TextGraySecondary
import com.example.ui.theme.TextWhitePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(
    apps: List<CloneableApp>,
    selectedPackages: Set<String>,
    currentCloneCount: Int,
    isVip: Boolean,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onAppClick: (String, String) -> Unit,
    onCancel: () -> Unit,
    onConfirmClone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxClones = if (isVip) 99 else 3
    val totalCount = currentCloneCount + selectedPackages.size

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Social", "Messaging", "Games")

    val filteredApps = remember(apps, searchQuery, selectedCategory) {
        apps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || app.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = Color(0xFFFBFBFD),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = modifier.testTag("app_picker_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            // Header title matching specification: "Add to Multiclonix Space"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.add_to_multiclonix,
                        totalCount.toString(),
                        if (isVip) "∞" else maxClones.toString()
                    ),
                    color = Color(0xFF1E88E5),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("add_sheet_title")
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_apps),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F3F5),
                    unfocusedContainerColor = Color(0xFFF1F3F5),
                    focusedIndicatorColor = Color(0xFF1E88E5),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF212529),
                    unfocusedTextColor = Color(0xFF212529)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_apps_input")
            )

            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E88E5),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFE9ECEF),
                            labelColor = Color(0xFF495057)
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            // App Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        color = EmeraldCloneGreen
                    )
                } else if (filteredApps.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_apps_found),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppPickerItem(
                                app = app,
                                isSelected = selectedPackages.contains(app.packageName),
                                onClick = { onAppClick(app.packageName, app.appName) },
                                onToggleCheck = { onToggleSelect(app.packageName) }
                            )
                        }
                    }
                }
            }

            // Bottom Buttons: Cancel and bright green Clone
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF495057)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCED4DA))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_add_button")
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onConfirmClone,
                    enabled = selectedPackages.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldCloneGreen,
                        contentColor = Color.White,
                        disabledContainerColor = EmeraldCloneGreen.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("confirm_clone_button")
                ) {
                    Text(
                        text = stringResource(R.string.clone),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for AddAppsBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppsBottomSheet(
    apps: List<CloneableApp>,
    selectedPackages: Set<String>,
    currentCloneCount: Int,
    isVip: Boolean,
    isLoading: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onAppClick: ((String, String) -> Unit)? = null,
    onCancel: () -> Unit,
    onConfirmClone: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppPickerBottomSheet(
        apps = apps,
        selectedPackages = selectedPackages,
        currentCloneCount = currentCloneCount,
        isVip = isVip,
        isLoading = isLoading,
        searchQuery = searchQuery,
        onSearchChange = onSearchChange,
        onToggleSelect = onToggleSelect,
        onAppClick = onAppClick ?: { pkg, _ -> onToggleSelect(pkg) },
        onCancel = onCancel,
        onConfirmClone = onConfirmClone,
        modifier = modifier
    )
}

@Composable
fun AppPickerItem(
    app: CloneableApp,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleCheck: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        getAppIconBitmap(context, app.packageName)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("app_picker_item_${app.packageName}")
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE9ECEF)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier.size(46.dp)
                    )
                } else {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = Color(0xFF1E88E5),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Selection Circle Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) EmeraldCloneGreen else Color(0xFFD6DBDF))
                    .border(
                        1.5.dp,
                        if (isSelected) EmeraldCloneGreen else Color.White,
                        CircleShape
                    )
                    .clickable {
                        if (onToggleCheck != null) onToggleCheck() else onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.appName,
            color = Color(0xFF212529),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
