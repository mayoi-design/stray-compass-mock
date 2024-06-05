package com.example.stray_compass

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// SensorManagerを単体で宣言することは可能？
// Activityを継承せずに…
@SuppressLint("StaticFieldLeak")
class MainActivityViewModel(
    //val sensorActivity: MainActivity
): ViewModel() {
    private var _rotationAngle by mutableStateOf(FloatArray(3))
    val rotationAngle = _rotationAngle

    init {
        viewModelScope.launch {
            while(true) {
                updateOrientation()
                delay(100)
            }
        }
    }

    fun updateOrientation() {
        //sensorActivity.updateOrientationAngles()
        //_rotationAngle = sensorActivity.orientationAngles
    }
}