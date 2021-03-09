package com.example.helpcity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.recyclerview_note.*

class NewNoteActivity : AppCompatActivity() {

    private lateinit var newNoteTitle: EditText
    private lateinit var newNoteDescription: EditText
    private lateinit var newNoteTriggerButton: Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        //Toolbar
        setSupportActionBar(findViewById(R.id.new_note_toolbar))
        supportActionBar?.setTitle(R.string.new_note)

        newNoteTitle = findViewById(R.id.new_note_title)
        newNoteDescription = findViewById(R.id.new_note_description)
        newNoteTriggerButton = findViewById(R.id.new_note_button)

        newNoteTriggerButton.setOnClickListener {
            Toast.makeText(this, "Olá isto é um teste!", Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        const val EXTRA_REPLY = "com.example.helpcity.REPLY"
    }
}