package com.kkyoungs.recordapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_uri")
class RecordUri(@PrimaryKey val uri:String)

