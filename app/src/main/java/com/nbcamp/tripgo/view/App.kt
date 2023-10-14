package com.nbcamp.tripgo.view

import android.app.Application
import com.nbcamp.tripgo.util.PreferenceUtils

class App : Application() {
    override fun onCreate() {
        prefs = PreferenceUtils(applicationContext)
        super.onCreate()
    }

    companion object {
        lateinit var prefs: PreferenceUtils
    }
}
