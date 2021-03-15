package com.example.helpcity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.helpcity.adapters.NoteListAdapter
import com.example.helpcity.viewModel.NoteViewModel


class EditNoteActivity : AppCompatActivity() {

    private lateinit var editTitleTextView: EditText
    private lateinit var editDescriptionTextView: EditText

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)


        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        setSupportActionBar(findViewById(R.id.edit_note_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = intent.getStringExtra(NoteListAdapter.noteTitle)
        }

        editTitleTextView = findViewById(R.id._editNoteTitleText)
        editDescriptionTextView = findViewById(R.id._editNoteDescriptionText)

        editTitleTextView.setText(intent.getStringExtra(NoteListAdapter.noteTitle))
        editDescriptionTextView.setText(intent.getStringExtra(NoteListAdapter.noteDescription))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_edit_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_save_note -> {
                editNote()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editNote() {

        if (editTitleTextView.text.toString().isEmpty() || editDescriptionTextView.text.toString()
                .isEmpty()
        ) {
            Toast.makeText(this, R.string.note_not_edited, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val noteId = intent.getStringExtra(NoteListAdapter.noteId)
            // Log.e("WTF", noteId.toString());

            noteViewModel.updateById(
                editTitleTextView.text.toString(),
                editDescriptionTextView.text.toString(),
                noteId.toString()
            )

            Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show()
        }
    }
}