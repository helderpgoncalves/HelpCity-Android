package com.example.helpcity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.adapters.NoteListAdapter
import com.example.helpcity.entities.Note
import com.example.helpcity.viewModel.NoteViewModel
import kotlinx.android.synthetic.main.activity_edit_note.*

class EditNoteActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: NoteListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        _noteTitle.text = intent.getStringExtra("title").toString()
        _noteDesc.text = intent.getStringExtra("description").toString()
        _noteId.text = intent.getStringExtra("id").toString()

        setSupportActionBar(findViewById(R.id.edit_note_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = _noteTitle.text
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_note -> {
                // TODO
                Toast.makeText(this, "EDIT", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete_note -> {
                // TODO
                Toast.makeText(this, _noteId.text.toString(), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, _noteTitle.text.toString(), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, _noteDesc.text.toString(), Toast.LENGTH_SHORT).show()
                val i = Intent(this, NoteActivity::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.helpcity.REPLY"
    }
}