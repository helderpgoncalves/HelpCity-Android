package com.example.helpcity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupLoginOrLogoutButton()

        notes_card_view.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        map_card_view.setOnClickListener {
            if (AppPreferences.isLogin) {
                startActivity(Intent(this, MapActivity::class.java))
            } else {
                Toast.makeText(this@MainActivity, R.string.loginToGoMap, Toast.LENGTH_SHORT).show()
            }
        }

        profile_card_view.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        settings_card_view.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupLoginOrLogoutButton() {
        if (AppPreferences.isLogin) {
            welcome.text = AppPreferences.username
            icon_profile!!.setImageResource(R.drawable.ic_baseline_exit_to_app_24)
            login_logout_text!!.setText(R.string.logout)
        } else {
            icon_profile!!.setImageResource(R.drawable.ic_baseline_person_24)
            login_logout_text!!.setText(R.string.login)
        }
    }
}