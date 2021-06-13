package com.example.busstopreminder.data.point

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PointDTO(
    val latitude: Double,
    val longitude: Double
): Parcelable