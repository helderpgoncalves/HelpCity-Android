package com.example.helpcity.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "note_table")
class Note(@ColumnInfo(name = "title") val title: String)
