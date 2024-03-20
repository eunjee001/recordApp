package com.kkyoungs.simpleRecorder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_uri")
class RecordUri(val uri:String){
    @PrimaryKey (autoGenerate = true) var id:Int = 0
}

