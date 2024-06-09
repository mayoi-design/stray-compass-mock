package com.example.stray_compass

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService: Service() {
    private val locationRequest: LocationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000
        ).build()

    private lateinit var locationProviderClient: FusedLocationProviderClient

    private val locationListener = LocationListener { p0 ->
        val currentLatitude = p0.latitude
        val currentLongitude = p0.longitude

        Log.d("Location", "${currentLatitude}, $currentLongitude")
        // Intentに乗せてMainActivityに通知する
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Location", "Entered onStartCommand()")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationListener,
                Looper.getMainLooper()
            )

            Log.d("Location", "Location Service has been started and the callback is attached")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // これでやろうと思ったけど、IntentでActivityにlat, lng流して
    // ActivityからViewModel通してUIに通知するのが良いかも
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

    override fun onDestroy() {
        locationProviderClient.removeLocationUpdates(locationListener)
        super.onDestroy()
    }
}