package com.example.busstopreminder.presentation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UISearchLocation(
    val name: String,
    val address: String?,
    val point: LocationPoint
): Parcelable

@Parcelize
data class LocationPoint(
    val latitude: Double,
    val longitude: Double
): Parcelable