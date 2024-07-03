package com.example.stray_compass

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.stray_compass.resource.mainActivityDebugTextPreference
import kotlinx.coroutines.runBlocking

class StrayCompassApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val ctx = this
        runBlocking {
            ctx.debugFeatureFlag.edit { flags ->
                flags[mainActivityDebugTextPreference] = true
            }
        }
    }
}

val Context.debugFeatureFlag: DataStore<Preferences> by preferencesDataStore(name = "DebugFeatureFlag")