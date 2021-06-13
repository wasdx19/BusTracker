package com.example.busstopreminder.presentation.alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.busstopreminder.R
import com.example.busstopreminder.common.constants.Constants.DEFAULT_LOCATION_LATITUDE
import com.example.busstopreminder.common.constants.Constants.DEFAULT_LOCATION_LONGITUDE
import com.example.busstopreminder.presentation.model.UISearchLocation
import com.example.busstopreminder.util.MapObjectSelectUtill
import com.example.busstopreminder.util.SearchSuggestionUtill
import com.example.busstopreminder.util.SearchUtill
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SuggestSession
import com.yandex.runtime.image.ImageProvider
import kotlinx.android.synthetic.main.activity_select_location.*

class SelectLocationActivity : AppCompatActivity(R.layout.activity_select_location) {

    private var searchManager: SearchManager? = null
    private var suggestSession: SuggestSession? = null

    private var suggestAdapter: ArrayAdapter<String>? = null
    private var suggestResult = arrayListOf<String>()

    private var searchSuggestionUtill: SearchSuggestionUtill? = null
    private var searchUtill: SearchUtill? = null

    private var mapObjectSelectUtill: MapObjectSelectUtill? = null

    private var startLocation: Point? = null
    private var isLocationDefined = false

    private var locationData: UISearchLocation? = null

    private val locationA: String?
        get() = intent.getStringExtra("LOCATION_A")

    private val locationB: String?
        get() = intent.getStringExtra("LOCATION_B")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMap()
        initMapConfigurations()
        moveCamera(Point(DEFAULT_LOCATION_LATITUDE, DEFAULT_LOCATION_LONGITUDE))
        setupSearch()
        setupSuggestion()
        setClickListeners()
        setupObjectSelect()
    }

    private fun initMap() {
        MapKitFactory.initialize(this)
        SearchFactory.initialize(this)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    private fun initMapConfigurations() {
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        suggestSession = searchManager?.createSuggestSession()
        searchSuggestionUtill = SearchSuggestionUtill(searchManager, suggestSession)
        searchUtill = SearchUtill(searchManager)
        mapObjectSelectUtill = MapObjectSelectUtill(mapView)
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

    private fun setupSearch() {
        searchUtill?.onSearchResult = { resultLocation ->
            val mapObjects = mapView.map.mapObjects

            mapObjects.clear()
            mapObjects.addPlacemark(
                resultLocation,
                ImageProvider.fromResource(this, R.drawable.location_pin)
            )

            if (locationView.isFocused) {
                startLocation = Point(resultLocation.latitude, resultLocation.longitude)
            }

            moveCamera(resultLocation)
        }

        searchUtill?.onSearchAddressResult = {
            setLocationInfoView(it)
        }

        searchUtill?.onSearchErrorAccured = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setClickListeners() {
        backIcon.setOnClickListener {
            finish()
        }
        saveLocation.setOnClickListener {
            if(locationA != null){
                setupIntent("LocationA")
            }else if(locationB != null){
                setupIntent("LocationB")
            }
        }
    }

    private fun setupIntent(key: String){
        val intent = Intent()
        intent.putExtra(key, locationData)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setupSuggestionClickListener() {
        locationView.setOnClickListener {
            isLocationDefined = false
        }

        locationSuggestionList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (locationView.isFocused) {
                    isLocationDefined = true
                    doOnSuggestionSelect(
                        locationView,
                        position,
                        locationView.text.toString()
                    )
                }
            }

        locationView.doAfterTextChanged {
            searchResultView.visibility = View.GONE
            doOnSearchSuggestionAct(isLocationDefined, it.toString())
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

    private fun setupObjectSelect() {
        mapObjectSelectUtill?.onTapResult = {
            setLocationInfoView(it)
        }
        mapObjectSelectUtill?.onRetap = {
            searchResultView.visibility = View.GONE
        }
    }

    private fun setLocationInfoView(data: UISearchLocation) {
        locationData = data
        searchResultView.visibility = View.VISIBLE
        locationName.text = data.name
        locationAddress.text = data.address
    }

    private fun moveCamera(point: Point) {
        mapView.map.move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )
    }
}