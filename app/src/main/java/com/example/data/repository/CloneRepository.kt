package com.example.data.repository

import com.example.data.dao.CloneDao
import com.example.data.model.CloneItem
import kotlinx.coroutines.flow.Flow

class CloneRepository(private val cloneDao: CloneDao) {
    val allClones: Flow<List<CloneItem>> = cloneDao.getAllClones()
    val cloneCount: Flow<Int> = cloneDao.getCloneCount()

    suspend fun getCloneById(id: Long): CloneItem? = cloneDao.getCloneById(id)

    suspend fun getClonesByPackage(packageName: String): List<CloneItem> =
        cloneDao.getClonesByPackage(packageName)

    suspend fun insertClone(clone: CloneItem): Long = cloneDao.insertClone(clone)

    suspend fun updateClone(clone: CloneItem) = cloneDao.updateClone(clone)

    suspend fun deleteClone(id: Long) = cloneDao.deleteClone(id)

    suspend fun updateLastLaunched(id: Long, timestamp: Long = System.currentTimeMillis()) =
        cloneDao.updateLastLaunched(id, timestamp)
}
