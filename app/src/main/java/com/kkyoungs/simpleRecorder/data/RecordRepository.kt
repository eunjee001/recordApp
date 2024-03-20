package com.kkyoungs.simpleRecorder.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao : RecordDao) {
    val allRecords : Flow<List<RecordUri>> = recordDao.getAlphabetizedWords()

    @WorkerThread
    suspend fun insert(recordUri : RecordUri){
        recordDao.insert(recordUri)
    }
}