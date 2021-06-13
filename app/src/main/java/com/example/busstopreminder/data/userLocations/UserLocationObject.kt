package com.example.busstopreminder.data.userLocations

import android.os.Parcelable
import com.example.busstopreminder.data.point.PointDTO
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserLocationsDTO(
    val userLocations: List<UserLocationItemDTO>
): Parcelable

@Parcelize
data class UserLocationItemDTO(
    val id: Int,
    val distance: Int,
    val startLocation: UserLocationPoints,
    val endLocation: UserLocationPoints
): Parcelable

@Parcelize
data class UserLocationPoints(
    val locationName: String,
    val locationPoint: PointDTO
): Parcelable