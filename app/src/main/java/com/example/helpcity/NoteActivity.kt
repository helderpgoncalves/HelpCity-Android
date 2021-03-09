package com.example.helpcity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.adapters.NoteListAdapter
import com.example.helpcity.entities.Note
import com.example.helpcity.viewModel.NoteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.recyclerview_note.*

class NoteActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private val newNoteActivityRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        setSupportActionBar(findViewById(R.id.noteListToolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

            //Recycler view
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotes)
            val adapter = NoteListAdapter(this)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            //View model
            noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
            noteViewModel.allNotes.observe(this, { notes ->
                notes?.let { adapter.setNotes(it) }
            })

            val fab = findViewById<FloatingActionButton>(R.id.notesFab)
            fab.setOnClickListener {
                val intent = Intent(this, NewNoteActivity::class.java)
                startActivityForResult(intent, newNoteActivityRequestCode)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == newNoteActivityRequestCode && resultCode == RESULT_OK) {
                data?.getStringArrayExtra(NewNoteActivity.EXTRA_REPLY)?.let {
                    val note = Note(noteTitle = it[0], noteDescription = it[1])
                    noteViewModel.insert(note)
                }
            } else {
                Toast.makeText(applicationContext, R.string.errorNote, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_delete_selected -> Toast.makeText(this, "Delete Selected", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
     }