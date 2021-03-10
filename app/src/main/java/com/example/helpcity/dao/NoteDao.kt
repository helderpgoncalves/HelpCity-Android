package com.example.helpcity.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.helpcity.entities.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM note ORDER BY noteId ASC") // ORDERED NOTES
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("UPDATE note SET noteTitle = :noteTitle, noteDescription = :noteDescription where noteId == :noteId")
    suspend fun updateById(noteTitle: String, noteDescription: String, noteId: String)

    @Query("DELETE FROM note")
    suspend fun deleteAll()

    @Query("SELECT * FROM note WHERE noteTitle == :noteTitle")
    fun getNoteByTitle(noteTitle: String) : Note

    @Query("DELETE FROM note WHERE noteId == :noteId")
    fun deleteById(noteId: String)

    @Query("Select * from note where noteDescription like  :desc OR noteTitle like :desc")
    fun getSearchResults(desc : String) : LiveData<List<Note>>
}