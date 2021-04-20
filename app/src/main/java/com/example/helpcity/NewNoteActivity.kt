package com.example.helpcity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NewNoteActivity : AppCompatActivity() {

    private lateinit var newNoteTitle: EditText
    private lateinit var newNoteDescription: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        //Toolbar
        setSupportActionBar(findViewById(R.id.new_note_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.new_note)
        }


        newNoteTitle = findViewById(R.id.new_note_title)
        newNoteDescription = findViewById(R.id.new_note_description)
        val button = findViewById<Button>(R.id.new_note_button)

        // SUBMIT NEW NOTE
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(newNoteTitle.text) || TextUtils.isEmpty(newNoteDescription.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val noteTitle = newNoteTitle.text.toString()
                val noteDescription = newNoteDescription.text.toString()

                replyIntent.putExtra(EXTRA_REPLY, arrayOf(noteTitle, noteDescription))

                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }

    }

    companion object {
        const val EXTRA_REPLY = "com.example.helpcity.REPLY"
    }
}