package com.example.busstopreminder.presentation.alarm

import android.content.Intent
import android.os.Bundle
import android.provider.Contacts
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.busstopreminder.R
import com.example.busstopreminder.common.pref.Preference
import com.example.busstopreminder.data.point.PointDTO
import com.example.busstopreminder.data.userLocations.UserLocationItemDTO
import com.example.busstopreminder.data.userLocations.UserLocationPoints
import com.example.busstopreminder.data.userLocations.UserLocationsDTO
import com.example.busstopreminder.presentation.model.UISearchLocation
import kotlinx.android.synthetic.main.activity_alarm_param.*
import org.koin.android.ext.android.inject

class AlarmParamActivity : AppCompatActivity(R.layout.activity_alarm_param) {

    private val pref by inject<Preference>()

    private var distance: String? = ""
    private lateinit var startLocation: UISearchLocation
    private lateinit var endLocation: UISearchLocation
    private lateinit var userLocations: UserLocationsDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setClickListeners()
    }

    private fun setupView() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.distance,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceSpinner.adapter = adapter
    }

    private fun setClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        chooseLocationA.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity()::class.java)
            intent.putExtra("LOCATION_A", "LOCATION_A")
            startActivityForResult(intent, 1)
        }
        chooseLocationB.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity()::class.java)
            intent.putExtra("LOCATION_B", "LOCATION_B")
            startActivityForResult(intent, 1)
        }
        saveButton.setOnClickListener {
            saveLocations()
        }
        distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                distance = distanceSpinner.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null)
            return
        else {
            if (data.extras?.containsKey("LocationA")!!) {
                val locationA = data.getParcelableExtra("LocationA") as UISearchLocation
                locationAText.text = locationA.name
                startLocation = locationA
            } else if (data.extras?.containsKey("LocationB")!!) {
                val locationB = data.getParcelableExtra("LocationB") as UISearchLocation
                locationBText.text = locationB.name
                endLocation = locationB
            }
        }
    }

    private fun saveLocations() {
        val locations = pref.getUserLocation()

        if (locations != null) {
            val location = arrayListOf<UserLocationItemDTO>()
            location.addAll(locations.userLocations)
            location.add(
                getUserItemLocation()
            )
            userLocations = UserLocationsDTO(
                userLocations = location
            )
        } else {
            userLocations = UserLocationsDTO(
                userLocations = mutableListOf<UserLocationItemDTO>().apply {
                    add(
                        getUserItemLocation()
                    )
                }
            )
        }

        pref.saveUserLocation(userLocations)
        finish()
    }

    private fun getUserItemLocation() =
        UserLocationItemDTO(
            id = 0,
            distance = distance?.toInt() ?: 0,
            startLocation = UserLocationPoints(
                locationName = startLocation.name,
                locationPoint = PointDTO(
                    latitude = startLocation.point.latitude,
                    longitude = startLocation.point.longitude,
                )
            ),
            endLocation = UserLocationPoints(
                locationName = endLocation.name,
                locationPoint = PointDTO(
                    latitude = endLocation.point.latitude,
                    longitude = endLocation.point.longitude,
                )
            ),
        )

}