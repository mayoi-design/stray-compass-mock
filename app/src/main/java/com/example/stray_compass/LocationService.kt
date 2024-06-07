package com.example.stray_compass

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

class LocationService(
    private val locationProviderClient: FusedLocationProviderClient
): Service() {
    val locationRequest: LocationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000
        ).build()

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun registerLocationUpdateCallback(listener: LocationListener) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 権限は上層でリクエストする
            return
        }

        locationProviderClient.requestLocationUpdates(
            locationRequest,
            listener,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates(listener: LocationListener) {
        locationProviderClient.removeLocationUpdates(listener)
    }
}