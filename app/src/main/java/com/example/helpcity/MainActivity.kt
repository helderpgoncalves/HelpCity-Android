package com.example.helpcity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        notes_card_view.setOnClickListener{
            startActivity(Intent(this, NoteActivity::class.java))
        }

        /*
        TODO
         */
    }



}