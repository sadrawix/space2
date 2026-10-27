package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clones",
    indices = [Index(value = ["packageName", "engineType", "instanceIndex"], unique = true)]
)
data class CloneItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val originalAppName: String,
    val displayName: String,
    val engineType: String,
    val instanceIndex: Int = 2,
    val badgeLabel: String = "CLONE",
    val badgeColorHex: String = "#2ED573",
    val isLocked: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val lastLaunchedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val sandboxUuid: String = "",
    val sandboxPath: String = ""
)
