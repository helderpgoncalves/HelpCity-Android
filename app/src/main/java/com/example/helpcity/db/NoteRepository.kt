package com.example.helpcity.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.helpcity.dao.NoteDao
import com.example.helpcity.entities.Note

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getNotes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun updateById(noteId: String, noteTitle: String, noteDescription: String) {
        noteDao.updateById(noteTitle, noteDescription, noteId)
    }

    suspend fun deleteById(noteId: String) {
        noteDao.deleteById(noteId)
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    @WorkerThread
    fun search(desc : String) : LiveData<List<Note>>{
        return noteDao.getSearchResults(desc)
    }
}