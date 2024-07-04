package com.example.stray_compass.resource

import android.location.Location
import com.example.stray_compass.doubleToLocation

data class Destination(
    val name:String,
    val location: Location
)

val destinationList = listOf(
    Destination("函館山", doubleToLocation(41.7603614, 140.7045022)),
    Destination("立待岬", doubleToLocation(41.7452515, 140.7215674)),
    Destination("函館熱帯植物園", doubleToLocation(41.7740046, 140.7895020)),
    Destination("新函館北斗駅", doubleToLocation(41.9046990, 140.6483768)),
    Destination("新中野ダム", doubleToLocation(41.8706050, 140.7868001)),
    Destination("北海道東照宮", doubleToLocation(41.8386536, 140.7850566)),
    Destination("蔦屋書店", doubleToLocation(41.8306748, 140.7356658))
)