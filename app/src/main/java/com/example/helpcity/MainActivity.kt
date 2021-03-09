package com.example.helpcity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.helpcity.fragments.MapFragment
import com.example.helpcity.fragments.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mapFragment = MapFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        map_card_view.setOnClickListener {
            Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show()
        }

        notes_card_view.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        profile_card_view.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }

        settings_card_view.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

}