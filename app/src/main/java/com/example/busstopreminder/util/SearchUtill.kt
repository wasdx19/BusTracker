package com.example.busstopreminder.util

import com.example.busstopreminder.presentation.model.LocationPoint
import com.example.busstopreminder.presentation.model.UISearchLocation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

class SearchUtill(
    private var searchManagerParam: SearchManager?
) : Session.SearchListener, CameraListener {

    private var searchManager: SearchManager? = null
    private var searchSession: Session? = null

    var onSearchErrorAccured: ((String) -> Unit)? = null
    var onSearchResult: ((Point) -> Unit)? = null
    var onSearchAddressResult: ((UISearchLocation) -> Unit)? = null

    init {
        searchManager = searchManagerParam
    }

    /**
     *  Для поиска точки
     */
    fun submitQuery(query: String, mapView: MapView) {
        searchSession = searchManager?.submit(
            query,
            VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    fun submitTapQuery(point: Point, mapView: MapView){
        searchSession = searchManager?.submit(
            point,
            11,
            SearchOptions(),
            this
        )
    }

    fun submitUriQuery(uri: String){
        searchSession = searchManager?.searchByURI(
            uri,
            SearchOptions(),
            this
        )
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Ошибка поиска"
        if (error is RemoteError)
            errorMessage = "удаленная ошибка поиска"
        else if (error is NetworkError)
            errorMessage = "сеть ошибка поиск"

        onSearchErrorAccured?.invoke(errorMessage)
    }

    override fun onSearchResponse(response: Response) {
        for (searchResult in response.collection.children) {
            val resultLocation =
                searchResult.obj!!.geometry[0].point
            if (resultLocation != null) {
                onSearchResult?.invoke(resultLocation)
                onSearchAddressResult?.invoke(
                    UISearchLocation(
                        name = searchResult.obj!!.name.orEmpty(),
                        address = searchResult.collection?.metadataContainer?.getItem(
                            BusinessObjectMetadata::class.java
                        )?.address?.formattedAddress,
                        point = LocationPoint(resultLocation.latitude, resultLocation.longitude)
                    )
                )
            }

        }
    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateSource,
        p3: Boolean
    ) {
        TODO("Not yet implemented")
    }

}