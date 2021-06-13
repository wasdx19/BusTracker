package com.example.busstopreminder.presentation.alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.example.busstopreminder.R
import com.example.busstopreminder.common.pref.Preference
import com.example.busstopreminder.data.userLocations.UserLocationItemDTO
import com.example.busstopreminder.presentation.alarm.viewBinder.UserLocationViewBinder
import kotlinx.android.synthetic.main.activity_alarm.*
import org.koin.android.ext.android.inject

class AlarmActivity : AppCompatActivity(R.layout.activity_alarm) {

    private val pref by inject<Preference>()

    private val multiTypeAdapter = MultiTypeAdapter().apply {
        register(UserLocationViewBinder{
            saveSelectedLocation(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClickListeners()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        setupList()
    }

    private fun setupView() {
        alarmRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = multiTypeAdapter
        }
    }

    private fun setupList(){
        multiTypeAdapter.apply {
            val list = pref.getUserLocation()?.userLocations
            if (list != null) {
                alarmRecycler.visibility = View.VISIBLE
                emptyAlarmView.visibility = View.GONE
                items = list
                notifyDataSetChanged()
            } else {
                alarmRecycler.visibility = View.GONE
                emptyAlarmView.visibility = View.VISIBLE
            }
        }
    }

    private fun setClickListeners() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        addNewAlarm.setOnClickListener {
            val intent = Intent(this, AlarmParamActivity()::class.java)
            startActivity(intent)
        }
    }

    private fun saveSelectedLocation(userLocationItemDTO: UserLocationItemDTO) {
        pref.setSelectedUserLocation(userLocationItemDTO)
    }
}