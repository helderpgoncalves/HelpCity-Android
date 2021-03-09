package com.example.helpcity.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.helpcity.entities.Note

@Dao
interface NoteDao {

    // CRUD

    @Query("SELECT * FROM note ORDER BY noteId ASC") // ORDERED NOTES
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Query("UPDATE note SET noteTitle = :noteTitle, noteDescription = :noteDescription where noteId == :noteId")
    suspend fun updateById(noteTitle: String, noteDescription: String, noteId: String)

    @Query("DELETE FROM note")
    suspend fun deleteAll()

    @Query("DELETE FROM note WHERE noteId == :noteId")
    suspend fun deleteById(noteId: String)

}