package com.kkyoungs.recordapp

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao : RecordDao) {
    val allRecords : Flow<List<RecordUri>> = recordDao.getAlphabetizedWords()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(recordUri : RecordUri){
        recordDao.insert(recordUri)
    }
}