package com.example.helpcity.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Int? = null,
    @ColumnInfo val noteTitle: String,
    @ColumnInfo val noteDescription: String
)
