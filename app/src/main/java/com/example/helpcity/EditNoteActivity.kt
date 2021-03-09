package com.example.helpcity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class EditNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        setSupportActionBar(findViewById(R.id.noteListToolbar))
    }
}