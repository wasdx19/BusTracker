package com.example.busstopreminder.util

import com.example.busstopreminder.presentation.model.UIBusLineData
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.SubpolylineHelper
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.*
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

class MasstransitRouterUtill : Session.RouteListener {

    private var router: MasstransitRouter? = null

    var onTransitErrorAccured: ((String) -> Unit)? = null
    var onTransitResultStroke: ((Pair<Int, Polyline>) -> Unit)? = null
    var transportLineList : ((List<UIBusLineData>) -> Unit)? = null

    var waypoints: ((List<Point>) -> Unit)? = null

    init {
        router = TransportFactory.getInstance().createMasstransitRouter()
    }

    fun requestRoutes(points: List<RequestPoint>) {
        val mapOptions = MasstransitOptions(
            arrayListOf<String>(),
            arrayListOf<String>(),
            TimeOptions()
        )

        router?.requestRoutes(points, mapOptions, this)
    }

    override fun onMasstransitRoutesError(error: Error) {
        var errorMessage = "ошибка"
        if (error is RemoteError) {
            errorMessage = "удаленная ошибка"
        } else if (error is NetworkError) {
            errorMessage = "ошибка сети"
        }
        onTransitErrorAccured?.invoke(errorMessage)
    }

    override fun onMasstransitRoutes(routes: MutableList<Route>) {
        val lineList = arrayListOf<UIBusLineData>()

        routes.forEach {

            if (routes.size > 0) {
                routes.forEach {
                    for (section in it.sections) {
                        drawSection(
                            section.metadata.data,
                            SubpolylineHelper.subpolyline(
                                it.geometry, section.geometry
                            )
                        )
                    }
                }

                routes.forEach { route ->
                    route.sections.forEach { section ->
                        section.metadata.data.transports?.forEach { transport ->
                            lineList.add(
                                UIBusLineData(
                                    busLine = transport.line.name,
                                    arrivalTime = "",
                                    geometry = route.geometry
                                )
                            )
                        }
                    }
                }
            }
        }
        transportLineList?.invoke(lineList)
    }

    private fun drawSection(
        data: SectionMetadata.SectionData,
        geometry: Polyline,
    ) {
        // Masstransit route section defines exactly one on the following
        // 1. Wait until public transport unit arrives
        // 2. Walk
        // 3. Transfer to a nearby stop (typically transfer to a connected
        //    underground station)
        // 4. Ride on a public transport
        // Check the corresponding object for null to get to know which
        // kind of section it is
        if (data.transports != null) {
            // A ride on a public transport section contains information about
            // all known public transport lines which can be used to travel from
            // the start of the section to the end of the section without transfers
            // along a similar geometry
//            for (transport in data.transports!!) {
//                // Some public transport lines may have a color associated with them
//                // Typically this is the case of underground lines
//                if (transport.line.style != null) {
//
//                    onTransitResultStroke?.invoke(
//                        Pair(
//                            transport.line.style!!.color!! or -0x1000000,
//                            geometry
//                        )
//                    )
//                    return
//                }
//            }

            // Let us draw bus lines in green and tramway lines in red
            // Draw any other public transport lines in blue
            val knownVehicleTypes: HashSet<String> = HashSet()
            knownVehicleTypes.add("bus")
            //knownVehicleTypes.add("tramway")
            for (transport in data.transports!!) {
                val sectionVehicleType = getVehicleType(transport, knownVehicleTypes)
                val isRecommended = getIsRecommended(transport)
                if (sectionVehicleType == "bus") {
                    onTransitResultStroke?.invoke(
                        Pair(-0xff0100, geometry) // Green
                    )
                    return
                }
                else if (sectionVehicleType == "tramway") {
                    onTransitResultStroke?.invoke(
                        Pair(-0x10000, geometry)  // Red
                    )
                    return
                }
            }
//            onTransitResultStroke?.invoke(
//                Pair(-0xffff01, geometry)  // Blue
//            )
        }
        else {
            // This is not a public transport ride section
            // In this example let us draw it in black
            onTransitResultStroke?.invoke(
                Pair(-0x1000000, geometry) // Black
            )
        }
    }

    private fun getVehicleType(
        transport: Transport,
        knownVehicleTypes: HashSet<String>
    ): String? {
        // A public transport line may have a few 'vehicle types' associated with it
        // These vehicle types are sorted from more specific (say, 'histroic_tram')
        // to more common (say, 'tramway').
        // Your application does not know the list of all vehicle types that occur in the data
        // (because this list is expanding over time), therefore to get the vehicle type of
        // a public line you should iterate from the more specific ones to more common ones
        // until you get a vehicle type which you can process
        // Some examples of vehicle types:
        // "bus", "minibus", "trolleybus", "tramway", "underground", "railway"
        for (type in transport.line.vehicleTypes) {
            if (knownVehicleTypes.contains(type)) {
                return type
            }
        }
        return null
    }

    private fun getIsRecommended(transport: Transport): Boolean{
        var recommended = false
        transport.transports.forEach {
            if(it.isRecommended){
                recommended = it.isRecommended
            }
        }
        return recommended
    }
}