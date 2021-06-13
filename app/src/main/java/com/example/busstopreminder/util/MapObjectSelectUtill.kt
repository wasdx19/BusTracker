package com.example.busstopreminder.util

import com.example.busstopreminder.presentation.model.LocationPoint
import com.example.busstopreminder.presentation.model.UISearchLocation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView


class MapObjectSelectUtill(
    private val map: MapView
) : GeoObjectTapListener, InputListener {

    private var mapView: MapView? = null

    var onTapResult: ((UISearchLocation) -> Unit)? = null
    var onRetap: (()-> Unit)? = null

    private var localGeoObjectTapEvent: GeoObjectTapEvent? = null

    var address: String? = null
    var name: String? = null

    init {
        mapView = map
        mapView!!.map.addTapListener(this)
        mapView!!.map.addInputListener(this)
    }

    override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
        val selectionMetadata: GeoObjectSelectionMetadata = geoObjectTapEvent
            .geoObject
            .metadataContainer
            .getItem<GeoObjectSelectionMetadata>(GeoObjectSelectionMetadata::class.java)

        mapView!!.map.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)


        val point = geoObjectTapEvent?.geoObject?.geometry[0].point

        onTapResult?.invoke(
            UISearchLocation(
                name = geoObjectTapEvent.geoObject.name.orEmpty(),
                address = geoObjectTapEvent.geoObject.descriptionText.orEmpty(),
                point = LocationPoint(point?.latitude!!, point?.longitude!!)
            )
        )

        return true
    }

    override fun onMapTap(map: Map, point: Point) {
        mapView?.map?.deselectGeoObject()
//        onRetap?.invoke()
    }

    override fun onMapLongTap(p0: Map, p1: Point) {

    }
}