package com.example.helpcity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NewOccurrenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_occurrence)

        //Toolbar
        setSupportActionBar(findViewById(R.id.new_occurrence_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.new_occurrence)
        }
    }
}

