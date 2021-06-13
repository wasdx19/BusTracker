package com.example.busstopreminder.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.example.busstopreminder.R
import com.example.busstopreminder.common.pref.Preference
import com.example.busstopreminder.presentation.main.viewBinder.BusLineViewBinder
import com.example.busstopreminder.service.BusTrackerService
import com.example.busstopreminder.util.MasstransitRouterUtill
import com.example.busstopreminder.util.SearchSuggestionUtill
import com.example.busstopreminder.util.SearchUtill
import com.google.android.gms.location.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity(), UserLocationObjectListener {

    private val pref by inject<Preference>()

    private lateinit var userLocationLayer: UserLocationLayer
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var userLocation: Point? = null
    private var startLocation: Point? = null
    private var endLocation: Point? = null

    private var mapObjectCollection: MapObjectCollection? = null

    private var searchManager: SearchManager? = null
    private var suggestSession: SuggestSession? = null

    private var suggestAdapter: ArrayAdapter<String>? = null
    private var suggestResult = arrayListOf<String>()

    private var isFromLocationDefined = false
    private var isToLocationDefined = false

    private var searchSuggestionUtill: SearchSuggestionUtill? = null
    private var searchUtill: SearchUtill? = null
    private var masstransitRouter: MasstransitRouterUtill? = null

    lateinit var polylineMapObject: PolylineMapObject

    private val animateHandler = Handler()

    private val multiTypeAdapter = MultiTypeAdapter().apply {
        register(BusLineViewBinder { item ->
            setupService()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMap()
        setContentView(R.layout.activity_main)

        setupMapView()
        setOnClickListener()

        initMapConfigurations()
        setupSearch()
        setupSuggestion()
        setupMassTransitRouter()
    }

    private fun initMap() {
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
        TransportFactory.initialize(this)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        makePermissionRequest()
        MapKitFactory.getInstance().onStart()
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

        mapObjectCollection = mapView.map.mapObjects.addCollection()
    }

    private fun initMapConfigurations() {
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        suggestSession = searchManager?.createSuggestSession()
        searchSuggestionUtill = SearchSuggestionUtill(searchManager, suggestSession)
        searchUtill = SearchUtill(searchManager)
        masstransitRouter = MasstransitRouterUtill()
    }

    private fun setupSuggestion() {
        suggestAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            suggestResult
        )
        locationSuggestionList.adapter = suggestAdapter

        searchSuggestionUtill?.onSuggestionErrorAccured = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        searchSuggestionUtill?.onSuggestionResult = {
            suggestResult.addAll(it)
            suggestAdapter?.notifyDataSetChanged()
            resultView.visibility = View.VISIBLE
        }

        setupSuggestionClickListener()
    }

    private fun setupService(){
        startService(Intent(this, BusTrackerService::class.java))
    }

    private fun setupSearch() {
        searchUtill?.onSearchResult = { resultLocation ->
            val mapObjects = mapView.map.mapObjects

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

        searchUtill?.onSearchErrorAccured = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupMassTransitRouter() {
        busList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = multiTypeAdapter
        }

        masstransitRouter?.onTransitErrorAccured = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        masstransitRouter?.onTransitResultStroke = {
            val (color, geometry) = it
            mapObjectCollection?.let {
                polylineMapObject = it.addPolyline(geometry)
                polylineMapObject.strokeColor = color
            }
        }

        masstransitRouter?.transportLineList = {
            busList.visibility = View.VISIBLE
            multiTypeAdapter.apply {
                items = it
                notifyDataSetChanged()
            }
        }
    }


    private fun setOnClickListener() {
        userLocationView.setOnClickListener {
            if (userLocation != null)
                moveCamera(userLocation!!)
        }

        goBtn.setOnClickListener {

            val points = arrayListOf<RequestPoint>()
            startLocation?.let { it1 ->
                points.add(RequestPoint(it1, RequestPointType.WAYPOINT, null))
            }
            //points.add(RequestPoint(Point(43.2401, 76.8950), RequestPointType.VIAPOINT, null))

            endLocation?.let { it1 ->
                points.add(RequestPoint(it1, RequestPointType.WAYPOINT, null))
            }
            //points.add(RequestPoint(Point(43.2401, 76.9266), RequestPointType.VIAPOINT, null))
            masstransitRouter?.requestRoutes(points)
            //addBusRouter()
        }
    }


    private fun setupSuggestionClickListener() {
        fromLocationView.setOnClickListener {
            isFromLocationDefined = false
        }

        toLocationView.setOnClickListener {
            isToLocationDefined = false
        }

        locationSuggestionList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (fromLocationView.isFocused) {
                    isFromLocationDefined = true
                    doOnSuggestionSelect(
                        fromLocationView,
                        position,
                        fromLocationView.text.toString()
                    )
                } else if (toLocationView.isFocused) {
                    isToLocationDefined = true
                    doOnSuggestionSelect(toLocationView, position, toLocationView.text.toString())
                }
            }

        toLocationView.doAfterTextChanged {
            doOnSearchSuggestionAct(isToLocationDefined, it.toString())
        }

        fromLocationView.doAfterTextChanged {
            doOnSearchSuggestionAct(isFromLocationDefined, it.toString())
        }
    }


    private fun doOnSearchSuggestionAct(defineValue: Boolean, text: String) {
        if (!defineValue && text.isNotEmpty()) {
            searchSuggestionUtill?.requestSuggest(text)
            resultView.visibility = View.INVISIBLE
        } else resultView.visibility = View.INVISIBLE
    }


    private fun doOnSuggestionSelect(view: EditText, position: Int, searchText: String) {
        val result = locationSuggestionList.getItemAtPosition(position) as String
        view.setText(result)
        resultView.visibility = View.INVISIBLE
        searchUtill?.submitQuery(searchText, mapView)
    }


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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //getUserLocation(true)
    }


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


    private fun moveCamera(point: Point) {
        mapView.map.move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )
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

    private fun addBusRouter() {
        val mapObjects = mapView.map.mapObjects

        val listPoint = arrayListOf<Point>(
            Point(
                43.2401,
                76.8950
            ),
            Point(
                43.2390,
                76.8950
            ),
            Point(
                43.2392,
                76.8977
            ),
        )

        val imageView = ImageView(this)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = params
        imageView.setImageResource(R.drawable.location_pin)
        val viewProvider = ViewProvider(imageView)

        var placeMark = mapObjects.addPlacemark(
            Point(
                43.2401,
                76.8950
            ), viewProvider
        )


        animateHandler.postDelayed({
            listPoint.forEach {
                animateHandler.postDelayed(object : Runnable {
                    override fun run() {
                        mapObjects.remove(placeMark)
                        placeMark = mapObjects.addPlacemark(it, viewProvider)
                        viewProvider.snapshot()
                        placeMark.setView(viewProvider)
                        //animateHandler.postDelayed(this, 500)
                    }
                }, 3000)
            }
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchManager = null
        searchSuggestionUtill = null
        searchUtill = null
        masstransitRouter = null
    }
}