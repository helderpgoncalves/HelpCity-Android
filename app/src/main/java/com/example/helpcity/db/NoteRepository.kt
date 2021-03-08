package com.example.helpcity.db

import androidx.annotation.WorkerThread
import com.example.helpcity.dao.NoteDao
import com.example.helpcity.entities.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository (private val noteDao: NoteDao){

    val allNotes: Flow<List<Note>> = noteDao.getOrderedNotes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }
}