package com.kkyoungs.recordapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RecordsApplication:Application() {
    val applicationScope  = CoroutineScope(SupervisorJob())
    val database by lazy { RecordRoomDB.getDatabase(this, applicationScope) }
    val repository by lazy { RecordRepository(database.recordDao()) }
}