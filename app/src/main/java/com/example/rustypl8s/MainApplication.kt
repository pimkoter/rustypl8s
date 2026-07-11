package com.example.rustypl8s

import android.app.Application
import uniffi.rustypl8s_core.WorkoutEngine
import java.io.File

class MainApplication : Application() {

    companion object {
        lateinit var engine: WorkoutEngine
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Path to persistent SQLite storage in the app's internal data folder
        val dbPath = File(filesDir, "rustypl8s.db").absolutePath
        
        try {
            // Initialize the Rust WorkoutEngine via UniFFI
            engine = WorkoutEngine(dbPath)
        } catch (e: Exception) {
            // In a real app, handle initialization failure (e.g., logging, crash reporting)
            e.printStackTrace()
        }
    }
}
