package com.example.stray_compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class MainActivityViewModel(
    private val sensorManager: SensorManager,
    initialTrippingState: TripState = TripState.Tripping(
        doubleToLocation(41.759167, 140.704444) // 函館山
    )
): ViewModel(), SensorEventListener {
    var currentLocation = doubleToLocation(0.0, 0.0)
        private set
    var currentLatitude by mutableDoubleStateOf(0.0)
        private set
    var currentLongitude by mutableDoubleStateOf(0.0)
        private set

    var trippingState: TripState = initialTrippingState
        private set

    var distanceToDestination by mutableDoubleStateOf(Double.POSITIVE_INFINITY)
        private set

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // メジアンフィルタは奇数で運用する。
    // nが偶数のとき、メジアンは中央の二要素の平均になるが、
    // 今回のユースケースでは平均の計算がバグにつながることがわかった
    private var azimuthMemo: List<Double> = List(7) { 0.0 }
    var azimuthInDegrees by mutableDoubleStateOf(0.0)
        private set

    var headdingTo: Double? by mutableStateOf(null)
        private set

    var navigationIconOffset by mutableStateOf(DoublePoint(0.0, 0.0))
        private set

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            // なんかポーリングを1秒くらいにするとローパスかけるより安定するみたいな話があった
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL
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

                azimuthMemo = azimuthMemo.drop(1) + listOf(azimuth.toDouble())
                val median = azimuthMemo.sorted()[3]

                azimuthInDegrees = Math.toDegrees(median)
                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360
                }

                val currentTrippingState = trippingState
                if (currentTrippingState is TripState.Tripping) {
                    headdingTo = getHeadingTo(
                        currentLocation = currentLocation,
                        destination = currentTrippingState.destination,
                        phi = azimuthInDegrees
                    )

                    navigationIconOffset = DoublePoint(
                        x = 400 * sin(
                            Math.toRadians(
                                floor(headdingTo ?: 0.0)
                            )
                        ),
                        y = -400 * cos(
                            Math.toRadians(
                                floor(headdingTo ?: 0.0)
                            )
                        ),
                    )
                }
                // Log.d("Azimuth", "方位角: $azimuthInDegrees 度")
            }
        }
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentLocation.latitude = latitude
        currentLocation.longitude = longitude

        // trippingStateの状態更新が重なる可能性があるのでここで一旦valに入れる。
        // (入れないとLintで怒られる)
        val currentTrippingState = trippingState
        if (currentTrippingState is TripState.Tripping) {
            distanceToDestination = currentTrippingState.destination.distanceTo(currentLocation).toDouble()
        }
    }

    private fun getHeadingTo(currentLocation: Location, destination: Location, phi: Double): Double? {
        // 現在地の取得がまだ終わっていないときはnullを返す。
        if (currentLocation.latitude == 0.0) {
            return null
        }

        val convertedLatitude = destination.latitude - currentLocation.latitude
        val convertedLongitude = destination.longitude - currentLocation.longitude
        Log.d("getHeaddingTo", "converted: ($convertedLatitude, $convertedLongitude)")
        val thetaInRad = atan2(convertedLatitude, convertedLongitude)
        Log.d("getHeddingTo", "phiInRad: $thetaInRad")
        val theta = Math.toDegrees(thetaInRad).mod(360.0)
        Log.d("getHeddingTo", "headTo: $theta")

        return (theta - phi).mod(360.0)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    fun registerSensorListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }
}