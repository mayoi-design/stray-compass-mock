package com.example.stray_compass

import android.location.Location

fun doubleToLocation(lat: Double, lng: Double): Location {
    val location = Location(null)
    location.latitude = lat
    location.longitude = lng

    return location
}