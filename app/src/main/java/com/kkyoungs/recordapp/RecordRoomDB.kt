package com.kkyoungs.recordapp

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [RecordUri::class], version = 1, exportSchema = false)
abstract class RecordRoomDB : RoomDatabase() {
    abstract fun recordDao() :RecordDao

    private class RecordDbCallback(private val scope:CoroutineScope) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                scope.launch {
                    var recordDao = it.recordDao()
                    recordDao.deleteAll()

                    var record = RecordUri("RecordExample_20240318_161145.mp4")
                    recordDao.insert(record)
                }
            }
        }
    }

    companion object{
        @Volatile
        private var INSTANCE : RecordRoomDB ?=null
        fun getDatabase(context : Context, scope :CoroutineScope) : RecordRoomDB{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordRoomDB::class.java,
                    "record_uri"
                )
                    .addCallback(RecordDbCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}