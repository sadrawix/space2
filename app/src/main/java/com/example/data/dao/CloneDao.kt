package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CloneItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CloneDao {
    @Query("SELECT * FROM clones ORDER BY createdAt ASC")
    fun getAllClones(): Flow<List<CloneItem>>

    @Query("SELECT * FROM clones WHERE id = :id LIMIT 1")
    suspend fun getCloneById(id: Long): CloneItem?

    @Query("SELECT * FROM clones WHERE packageName = :packageName")
    suspend fun getClonesByPackage(packageName: String): List<CloneItem>

    @Query("SELECT COUNT(*) FROM clones")
    fun getCloneCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(clone: CloneItem): Long

    @Update
    suspend fun updateClone(clone: CloneItem)

    @Query("DELETE FROM clones WHERE id = :id")
    suspend fun deleteClone(id: Long)

    @Query("UPDATE clones SET lastLaunchedAt = :time WHERE id = :id")
    suspend fun updateLastLaunched(id: Long, time: Long)
}
