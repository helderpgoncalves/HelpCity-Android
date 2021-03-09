package com.example.helpcity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.helpcity.fragments.MapFragment
import com.example.helpcity.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private val mapFragment = MapFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
    }

}