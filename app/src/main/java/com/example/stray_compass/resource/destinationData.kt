package com.example.stray_compass.resource

data class Destination(val name:String,val lat:Double,val lng:Double)

val destinationList = listOf(
    Destination("函館山",41.759167, 140.704444),
    Destination("立待岬",41.74564393712656, 140.72217385783742),
    Destination("植物園",41.77429723316786, 140.7895993198192),
    Destination("新函館北斗駅",41.904975472356206, 140.64916583040576)
)