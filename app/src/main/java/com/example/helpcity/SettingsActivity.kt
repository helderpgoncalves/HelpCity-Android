package com.example.helpcity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)


            //Notifications
            var notifications = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "notifications",
                false
            )

            var chooseType = PreferenceManager.getDefaultSharedPreferences(context).getString(
                "list_preference",
                "All"
            )

            AppPreferences.type = chooseType.toString()
            AppPreferences.notifications = notifications

            // Github
            val github: Preference? = findPreference("webpage")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/helderpgoncalves/HelpCity")
            github!!.intent = intent
        }
    }
}