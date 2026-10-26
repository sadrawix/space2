package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Merge with your existing CloneItem (keep any extra columns you already use).
 * Bump the Room version. The simplest migration is a destructive one, which
 * also wipes the fake clones created by the old fallback bug.
 */
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
    val isLocked: Boolean = false,
    val lastLaunchedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
