package com.example.busstopreminder.presentation.model

import com.yandex.mapkit.geometry.Polyline

data class UIBusLineData(
    val busLine: String,
    val arrivalTime: String,
    val geometry: Polyline
)