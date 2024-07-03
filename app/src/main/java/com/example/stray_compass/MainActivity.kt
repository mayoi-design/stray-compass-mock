package com.example.stray_compass

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.stray_compass.resource.Destination
import com.example.stray_compass.resource.destinationList
import com.example.stray_compass.resource.locationIntentAction
import com.example.stray_compass.resource.locationIntentLatitude
import com.example.stray_compass.resource.locationIntentLongitude
import kotlinx.coroutines.launch
import com.example.stray_compass.resource.mainActivityDebugTextPreference
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private val intentFilter: IntentFilter = IntentFilter()
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val bundle = intent.extras ?: return

            val currentLatitude = bundle.getDouble(locationIntentLatitude, -1.0)
            val currentLongitude = bundle.getDouble(locationIntentLongitude, -1.0)

            mainActivityViewModel.setCurrentLocation(currentLatitude, currentLongitude)
        }
    }


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
                        registerLocationBroadcastReceiver()
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
                            registerLocationBroadcastReceiver()
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

    private fun registerLocationBroadcastReceiver() {
        intentFilter.addAction(locationIntentAction)

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT < 33) {
            registerReceiver(
                broadcastReceiver,
                intentFilter
            )
        }
        else {
            registerReceiver(
                broadcastReceiver,
                intentFilter,
                RECEIVER_EXPORTED
            )
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
        Text(
            text = "Please allow the App to access your location",
            textAlign = TextAlign.Center
        )
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
    val ctx = LocalContext.current
    val debugFeatureFlag = ctx.debugFeatureFlag.data.map { preference ->
        preference[mainActivityDebugTextPreference] ?: true
    }.collectAsState(initial = false)

    val currentState = viewModel.trippingState
    Viewer(
        azimuth = viewModel.azimuthInDegrees,
        latitude = viewModel.currentLatitude,
        longitude = viewModel.currentLongitude,
        destination = if (currentState is TripState.Tripping) {
            currentState.destination
        } else {
            doubleToLocation(0.0, 0.0)
        },
        distance = viewModel.distanceToDestination,
        headTo = viewModel.headdingTo,
        navigationOffset = viewModel.navigationIconOffset,
        showBottomSheet = viewModel.showBottomSheet,
        onClickDestinationChoice = viewModel::changeDestination,
        changeBottomSheetState = viewModel::changeShowBottomSheet,
        debugFeatureFlag = debugFeatureFlag.value,
    )
}

@Composable
fun Viewer(
    azimuth: Double,
    latitude: Double,
    longitude: Double,
    destination: Location,
    distance: Double,
    headTo: Double?,
    navigationOffset: DoublePoint,
    showBottomSheet: Boolean,
    onClickDestinationChoice: (Destination) -> Unit,
    changeBottomSheetState: (Boolean) -> Unit,
    debugFeatureFlag: Boolean,
) {
    val scope = rememberCoroutineScope()
    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        if (debugFeatureFlag) {
            Text("azimuth: $azimuth")
            Text("latitude: $latitude")
            Text("longitude: $longitude")
            Text("Destination: ${destination.latitude}, ${destination.longitude}")
            Text("Distance: $distance")
            Text("headTo: $headTo")
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = {
            changeBottomSheetState(true)
        }){
            Text("目的地を変更")
        }

        Spacer(Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "REMAINING\n${distance.roundToInt() / 1000.0} [km]",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp
            )

            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = "Navigation",
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        translationX = navigationOffset.x.toFloat()
                        translationY = navigationOffset.y.toFloat()
                        rotationZ = headTo?.toFloat() ?: 0f
                    }
            )
        }

    }

    if (showBottomSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = {
               changeBottomSheetState(false)
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding()) {
                destinationList.forEach { destination ->
                    Button(
                        onClick = {
                            onClickDestinationChoice(destination)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                changeBottomSheetState(false)
                            }
                        }
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            destination.name,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}