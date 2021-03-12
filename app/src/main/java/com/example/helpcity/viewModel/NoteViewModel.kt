package com.example.helpcity.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.helpcity.adapters.NoteListAdapter.Companion.noteId
import com.example.helpcity.db.NoteDatabase
import com.example.helpcity.db.NoteRepository
import com.example.helpcity.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Flow

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>

    init {
        val notesDao = NoteDatabase.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }

    fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }

    fun updateById(noteTitle: String, noteDescription: String, noteId: String) =
        viewModelScope.launch{
            repository.updateById(noteTitle, noteDescription, noteId)
        }

    fun deleteNote(note: Note) =  viewModelScope.launch(Dispatchers.IO){
        repository.deleteNote(note)
    }

    fun updateNote(note: Note) =  viewModelScope.launch(Dispatchers.IO){
        repository.updateNote(note)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun searchForNotes(desc: String) : LiveData<List<Note>> {
        return repository.search(desc)
    }
}
