package com.example.busstopreminder.data.route

import android.os.Parcelable
import com.example.busstopreminder.data.point.PointDTO
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RoutesDTO(
    val routeItems: List<RouteItemDTO>
): Parcelable

@Parcelize
data class RouteItemDTO(
    val id: Int,
    val color: Int,
    val routeName: String,
    val busCount: Int,
    val polylinePoints: List<PointDTO>
): Parcelable