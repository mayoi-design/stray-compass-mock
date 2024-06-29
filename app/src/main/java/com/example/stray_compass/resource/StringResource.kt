package com.example.stray_compass.resource

import androidx.datastore.preferences.core.booleanPreferencesKey

const val locationIntentAction = "STRAY_COMPASS_LOCATION_BROADCAST"
const val locationIntentLatitude = "Latitude"
const val locationIntentLongitude = "Longitude"

val mainActivityDebugTextPreference = booleanPreferencesKey("MAINACTIVITY_DEBUG_TEXT_PREFERENCE")