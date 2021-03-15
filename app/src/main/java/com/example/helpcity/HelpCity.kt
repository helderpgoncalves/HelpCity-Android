package com.example.helpcity

import android.app.Application

class HelpCity : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}