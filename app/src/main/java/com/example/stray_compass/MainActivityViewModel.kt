package com.example.stray_compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainActivityViewModel(
    private val sensorManager: SensorManager
): ViewModel(), SensorEventListener {
    // ViewModelにListener継承するの頭良くないか？
    // これをGPSでもやりたい

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    var azimuthInDegrees by mutableIntStateOf(0)
        private set

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                accelerometer,
                1e6.toInt(),
            )
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            // なんかポーリングを1秒くらいにするとローパスかけるより安定するみたいな話があった
            sensorManager.registerListener(
                this,
                magnetometer,
                1e6.toInt()
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values
            }
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
        }

        if (gravity != null && geomagnetic != null) {
            // これデバイスによってめちゃくちゃ誤差出る。
            // 少なくともYoureinのP20 Liteは変な角度で持たないと正しい角度で測定してくれなかった

            val R = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, null, gravity, geomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                val azimuth = orientation[0]
                azimuthInDegrees = Math.toDegrees(azimuth.toDouble()).toInt()
                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360
                }
                Log.d("Azimuth", "方位角: $azimuthInDegrees 度")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    fun resisterSensorListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unresisterSensorListener() {
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }
}