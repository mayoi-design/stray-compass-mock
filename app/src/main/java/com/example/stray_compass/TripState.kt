package com.example.stray_compass

import android.location.Location
import androidx.compose.runtime.Stable

@Stable
sealed class TripState {
    data object BeforeTrip: TripState()
    data class Tripping(val destination: Location): TripState()
    data object AfterTrip: TripState()
}