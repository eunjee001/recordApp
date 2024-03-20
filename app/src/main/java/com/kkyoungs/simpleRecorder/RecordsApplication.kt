package com.kkyoungs.simpleRecorder

import android.app.Application
import com.kkyoungs.simpleRecorder.data.RecordRepository
import com.kkyoungs.simpleRecorder.data.RecordRoomDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RecordsApplication:Application() {
    //SupervisorJob 이 있으면 error가 생겨도  모든 과정이 끝까지 수행된다
    private val applicationScope  = CoroutineScope(SupervisorJob())
    private val database by lazy { RecordRoomDB.getDatabase(this, applicationScope) }
    val repository by lazy { RecordRepository(database.recordDao()) }
}