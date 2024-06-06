package com.example.stray_compass

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityViewModel = MainActivityViewModel(getSystemService(SENSOR_SERVICE) as SensorManager)
        setContent {
            MaterialTheme {
                Viewer(
                    mainActivityViewModel,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.resisterSensorListener()
    }

    override fun onPause() {
        super.onPause()
        mainActivityViewModel.unresisterSensorListener()
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