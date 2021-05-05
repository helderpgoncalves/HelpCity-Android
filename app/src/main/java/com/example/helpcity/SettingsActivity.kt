package com.example.helpcity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.settings_activity.*
import kotlin.math.roundToInt


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        setSupportActionBar(findViewById(R.id.settingsListToolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        switchNotification.isChecked = AppPreferences.notifications
        geofence_radius.value = AppPreferences.radius

        geofence_radius.setLabelFormatter { value: Float ->
            val nomeAntesDoSlider = resources.getString(R.string.range_of)
            val nomeMetros = resources.getString(R.string.meters)
            return@setLabelFormatter nomeAntesDoSlider.plus(" ${value.roundToInt()} ")
                .plus(nomeMetros)
        }

        geofence_radius.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
            AppPreferences.radius = slider.value
        })

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.notifications = isChecked
        }

    }
}