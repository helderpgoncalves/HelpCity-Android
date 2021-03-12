package com.example.helpcity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.adapters.NoteListAdapter
import com.example.helpcity.entities.Note
import com.example.helpcity.viewModel.NoteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

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


        val swipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val list: List<Note> = adapter.getNotes()
                noteViewModel.deleteNote(list[pos])
                Toast.makeText(context, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                adapter.notifyItemRemoved(pos)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        //View model
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }

        val fab = findViewById<FloatingActionButton>(R.id.notesFab)
        fab.setOnClickListener {
            val intent = Intent(this, NewNoteActivity::class.java)
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_delete_all -> {

                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton(R.string.yes) { _, _ ->
                    noteViewModel.deleteAll()
                    Toast.makeText(this, R.string.all_notes_cleared, Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton(R.string.no) { _, _ -> }
                builder.setTitle(R.string.delete_everything)
                builder.setMessage(R.string.delete_everything_confirmation)
                builder.create().show()

                true

            }
            else -> super.onOptionsItemSelected(item)
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

        val search = menu!!.findItem(R.id.search_notes)


        var searchView = search!!.actionView as androidx.appcompat.widget.SearchView

        searchView = search.actionView as androidx.appcompat.widget.SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.
        SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    getNotesFromDB(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    getNotesFromDB(newText)
                }
                return true
            }
        })
        return true
    }

    private fun getNotesFromDB(searchText: String) {
        var searchText = searchText
        searchText = "%$searchText%"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotes)

        val adapter = NoteListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //View model
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.searchForNotes(desc = searchText).observe(this) { notes ->
            notes.let { adapter.setNotes(it) }
        }
    }
}