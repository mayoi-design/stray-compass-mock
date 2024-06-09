package com.example.stray_compass

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityViewModel = MainActivityViewModel(getSystemService(SENSOR_SERVICE) as SensorManager)

        val locationServiceIntent = Intent(
            this,
            LocationService::class.java
        )
        val ctx: Context = this
        setContent {
            MaterialTheme {
                LaunchedEffect(Unit) {
                    if (ActivityCompat.checkSelfPermission(ctx, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ctx, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startService(locationServiceIntent)
                    }
                }

                var permitted by remember { mutableStateOf(
                    ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    when {
                        permissions.getOrDefault(ACCESS_FINE_LOCATION, false)
                                && permissions.getOrDefault(ACCESS_COARSE_LOCATION, false)
                        -> {
                            permitted = true
                            startService(locationServiceIntent)
                        }
                    }
                }

                if (!permitted) {
                    PermissionRequestView(
                        permissionRequest = launcher,
                    )
                }
                else {
                    Viewer(
                        mainActivityViewModel,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.registerSensorListener()
    }

    override fun onPause() {
        super.onPause()
        mainActivityViewModel.unregisterSensorListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, LocationService::class.java))
    }
 }

@Composable
fun PermissionRequestView(
    permissionRequest: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Please allow the App to access your location")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            permissionRequest.launch(
                listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).toTypedArray()
            )
        }) {
            Text("Tap to Allow")
        }
    }
}

@Composable
fun Viewer(
    viewModel: MainActivityViewModel
) {
    Viewer(azimuth = viewModel.azimuthInDegrees)
}

@Composable
fun Viewer(
    azimuth: Int,
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("azimuth: $azimuth")
    }
}