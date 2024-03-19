package com.kkyoungs.recordapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM record_uri ORDER BY uri ASC")
    fun getUri():List<RecordUri>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(uri: RecordUri)

    @Query("DELETE FROM record_uri")
    suspend fun deleteAll()

    @Query("SELECT * FROM record_uri ORDER BY uri ASC")
    fun getAlphabetizedWords(): Flow<List<RecordUri>>
}