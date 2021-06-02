package com.example.busstopreminder.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import com.example.busstopreminder.R
import com.google.android.gms.location.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.SubpolylineHelper
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.search.*
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SuggestSession.SuggestListener
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.min


class MainActivity : AppCompatActivity(), UserLocationObjectListener,
    com.yandex.mapkit.transport.masstransit.Session.RouteListener,
    SuggestListener, com.yandex.mapkit.search.Session.SearchListener,
    SummarySession.SummaryListener {


    private lateinit var userLocationLayer: UserLocationLayer
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var userLocation: Point? = null

    private var startLocation: Point? = null
    private var endLocation: Point? = null

    private var mapObjectCollection: MapObjectCollection? = null
    private var router: MasstransitRouter? = null

    private var searchManager: SearchManager? = null
    private var suggestSession: SuggestSession? = null
    private var suggestAdapter: ArrayAdapter<String>? = null
    private var searchSession: Session? = null
    private var suggestResult = arrayListOf<String>()

    private var pointCenter = Point(55.75, 37.62)
    private var boxSize = 0.2

    private var isFromLocationDefined = false
    private var isToLocationDefined = false

    private val boundingBox = BoundingBox(
        Point(pointCenter.latitude - boxSize, pointCenter.longitude - boxSize),
        Point(pointCenter.latitude + boxSize, pointCenter.longitude + boxSize)
    )

    private val suggestionOption = SuggestOptions().setSuggestTypes(
        SuggestType.GEO.value or SuggestType.BIZ.value or SuggestType.TRANSIT.value
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
        TransportFactory.initialize(this)
        setContentView(R.layout.activity_main)

        setupMapView()
        setOnClickListener()

        /**
         *  Настриваем подсказок для поиска
         */
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        suggestSession = searchManager?.createSuggestSession()

        /**
         *  Адаптер для списка подсказок
         */
        suggestAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            suggestResult
        )
        locationSuggestionList.adapter = suggestAdapter
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        makePermissionRequest()
        MapKitFactory.getInstance().onStart()
    }

    /**
     *  Обработка кликов
     */
    private fun setOnClickListener() {
        userLocationView.setOnClickListener {
            if (userLocation != null)
                moveCamera(userLocation!!)
        }

        goBtn.setOnClickListener {
            /**
             *  Настраиваем маршрут
             */
            mapObjectCollection = mapView.map.mapObjects.addCollection()
            val mapOptions = MasstransitOptions(
                arrayListOf<String>(),
                arrayListOf<String>(),
                TimeOptions()
            )

            val points = arrayListOf<RequestPoint>()
            startLocation?.let { it1 ->
                points.add(RequestPoint(it1, RequestPointType.WAYPOINT, null))
            }
            endLocation?.let { it1 ->
                points.add(RequestPoint(it1, RequestPointType.WAYPOINT, null))
            }
            router = TransportFactory.getInstance().createMasstransitRouter()
            router?.requestRoutes(points, mapOptions, this)
        }

        fromLocationView.setOnClickListener {
            isFromLocationDefined = false
        }

        toLocationView.setOnClickListener {
            isToLocationDefined = false
        }

        fromLocationView.doAfterTextChanged {
            if (!isFromLocationDefined && it.toString().isNotEmpty())
                requestSuggest(fromLocationView.text.toString())
            else
                resultView.visibility = View.INVISIBLE
        }

        toLocationView.doAfterTextChanged {
            if (!isToLocationDefined && it.toString().isNotEmpty())
                requestSuggest(toLocationView.text.toString())
            else
                resultView.visibility = View.INVISIBLE
        }

        locationSuggestionList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (fromLocationView.isFocused) {
                    val result = locationSuggestionList.getItemAtPosition(position) as String
                    isFromLocationDefined = true
                    fromLocationView.setText(result)
                    resultView.visibility = View.INVISIBLE
                    submitQuery(fromLocationView.text.toString())
                } else if (toLocationView.isFocused) {
                    val result = locationSuggestionList.getItemAtPosition(position) as String
                    isToLocationDefined = true
                    toLocationView.setText(result)
                    resultView.visibility = View.INVISIBLE
                    submitQuery(toLocationView.text.toString())
                }
            }
    }

    /**
     *  Запрашиваем доступ на местоположение пользователя
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun makePermissionRequest() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 225
            )
            return
        }
    }

    /**
     *  Если у нас есть доступ то получаем координаты пользователя
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //getUserLocation(true)
    }

    /**
     *  Здесь получаем координаты пользователя
     */
    @SuppressLint("MissingPermission")
    private fun getUserLocation(isPermission: Boolean) {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient?.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let {
                    userLocation = Point(it.latitude, it.longitude)
                    if (userLocation != null)
                        moveCamera(userLocation!!)
                }
            }
        }, null)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    private fun setupMapView() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        userLocationLayer.setObjectListener(this)
    }

    /**
     *  Меняем фокус камеры на нужные координаты
     */
    private fun moveCamera(point: Point) {
        mapView.map.move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )
    }

    /**
     *  Для поиска точки
     */
    private fun submitQuery(query: String) {
        searchSession = searchManager?.submit(
            query,
            VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    private fun requestSuggest(query: String) {
        resultView.visibility = View.INVISIBLE
        suggestSession?.suggest(query, boundingBox, suggestionOption, this)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )

        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                this, R.drawable.ic_pin_user
            )
        )

        val pinIcon: CompositeIcon = userLocationView.pin.useCompositeIcon()

        pinIcon.setIcon(
            "icon",
            ImageProvider.fromResource(this, R.drawable.ic_hand),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(0f)
                .setScale(1f)
        )

        pinIcon.setIcon(
            "pin",
            ImageProvider.fromResource(this, R.drawable.ic_push_pin),
            IconStyle().setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
                .setScale(0.5f)
        )

        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001

        mapView.map.move(
            CameraPosition(userLocationView.pin.geometry, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )

    }

    override fun onObjectRemoved(p0: UserLocationView) {
        // do nothing
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
        // do nothing
    }

    override fun onMasstransitRoutes(routes: MutableList<Route>) {
        // In this example we consider first alternative only
        if (routes.size > 0) {
            routes.forEach {
                for (section in it.sections) {
                    drawSection(
                        section.metadata.data,
                        SubpolylineHelper.subpolyline(
                            it.geometry, section.geometry
                        )
                    )
                    section.metadata.data.transports?.let { it1 ->
                        setLineList(it1)
                    }
                }
            }
        }
    }


    override fun onMasstransitRoutesError(error: Error) {
        var errorMessage = "ошибка"
        if (error is RemoteError) {
            errorMessage = "удаленная ошибка"
        } else if (error is NetworkError) {
            errorMessage = "ошибка сети"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setLineList(transports: MutableList<Transport>) {
        for (transport in transports){

        }
    }

    private fun drawSection(
        data: SectionMetadata.SectionData,
        geometry: Polyline
    ) {
        // Draw a section polyline on a map
        // Set its color depending on the information which the section contains
        lateinit var polylineMapObject: PolylineMapObject
        mapObjectCollection?.let {
            polylineMapObject = it.addPolyline(geometry)
        }

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
            for (transport in data.transports!!) {
                // Some public transport lines may have a color associated with them
                // Typically this is the case of underground lines
                if (transport.line.style != null) {
                    polylineMapObject.strokeColor = transport.line.style!!.color!! or -0x1000000
                    return
                }
            }

            val lineList = arrayListOf<String>()
            for (transport in data.transports!!) {
                lineList.add(transport.line.name)
            }

            // Let us draw bus lines in green and tramway lines in red
            // Draw any other public transport lines in blue
            val knownVehicleTypes: HashSet<String> = HashSet()
            knownVehicleTypes.add("bus")
            knownVehicleTypes.add("tramway")
            for (transport in data.transports!!) {
                val sectionVehicleType = getVehicleType(transport, knownVehicleTypes)
                if (sectionVehicleType == "bus") {
                    polylineMapObject.strokeColor = -0xff0100 // Green
                    return
                }
//                else if (sectionVehicleType == "tramway") {
//                    polylineMapObject.strokeColor = -0x10000 // Red
//                    return
//                }
            }
            polylineMapObject.strokeColor = -0xffff01 // Blue
        } else {
            // This is not a public transport ride section
            // In this example let us draw it in black
            polylineMapObject.strokeColor = -0x1000000 // Black
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

    private fun isRecommended(
        transport: Transport
    ): Transport.TransportThread? {
        for (va in transport.transports) {
            if (va.isRecommended) {
                return va
            }
        }
        return null
    }

    /**
     *  Ответ от подсказок
     */
    override fun onResponse(suggest: MutableList<SuggestItem>) {
        suggestResult.clear()
        for (i in 0 until min(5, suggest.size)) {
            suggestResult.add(suggest.get(i).displayText.orEmpty())
        }
        suggestAdapter?.notifyDataSetChanged()
        resultView.visibility = View.VISIBLE
    }

    /**
     * Ответ от результата поиска
     */
    override fun onSearchResponse(response: Response) {
        val mapObjects = mapView.map.mapObjects
        for (searchResult in response.collection.children) {
            val resultLocation =
                searchResult.obj!!.geometry[0].point
            if (resultLocation != null) {
                mapObjects.addPlacemark(
                    resultLocation,
                    ImageProvider.fromResource(this, R.drawable.location_pin)
                )

                if (fromLocationView.isFocused) {
                    startLocation = Point(resultLocation.latitude, resultLocation.longitude)
                } else if (toLocationView.isFocused) {
                    endLocation = Point(resultLocation.latitude, resultLocation.longitude)
                }

                moveCamera(resultLocation)
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Ошибка поиска"
        if (error is RemoteError) {
            errorMessage = "удаленная ошибка поиска"
        } else if (error is NetworkError) {
            errorMessage = "сеть ошибка поиск"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: Error) {
        var errorMessage = "Ошибка подсказки"
        if (error is RemoteError) {
            errorMessage = "удаленная ошибка подсказки"
        } else if (error is NetworkError) {
            errorMessage = "сеть ошибка подсказки"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onMasstransitSummaries(p0: MutableList<Summary>) {
        p0.forEach {
            it.estimation?.arrivalTime?.value
        }
    }

    override fun onMasstransitSummariesError(p0: Error) {
        TODO("Not yet implemented")
    }
}