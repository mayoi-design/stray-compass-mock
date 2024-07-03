package com.example.stray_compass.resource

import android.location.Location
import com.example.stray_compass.doubleToLocation

data class Destination(
    val name:String,
    val location: Location
)

val destinationList = listOf(
    Destination("函館山", doubleToLocation(41.759167, 140.704444)),
    Destination("立待岬", doubleToLocation(41.74564393712656, 140.72217385783742)),
    Destination("新函館北斗駅", doubleToLocation(41.904975472356206, 140.64916583040576)),
    Destination("新中野ダム", doubleToLocation(41.87060500660374, 140.78680017848293)),
    Destination("北海道東照宮", doubleToLocation(41.839274713428516, 140.78669408732782)),
    Destination("蔦屋書店", doubleToLocation(41.83622834602887, 140.7494532874035))
)