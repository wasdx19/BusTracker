package com.example.busstopreminder.presentation.actions

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.busstopreminder.R
import com.example.busstopreminder.common.pref.Preference
import com.example.busstopreminder.presentation.about.AboutActivity
import com.example.busstopreminder.presentation.alarm.AlarmActivity
import com.example.busstopreminder.presentation.main.MainActivity
import com.example.busstopreminder.service.BusTrackerService
import kotlinx.android.synthetic.main.activity_actions.*
import org.koin.android.ext.android.inject

class ActionsActivity : AppCompatActivity(R.layout.activity_actions){

    private val pref by inject<Preference>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnclickListeners()
    }

    override fun onResume() {
        super.onResume()
        setInfoLayout()
    }

    private fun setOnclickListeners() {
        startDirection.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        alarm.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
        about.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        setAlarm.setOnClickListener {
            startService(Intent(this, BusTrackerService::class.java))
        }
    }

    private fun setInfoLayout(){
        val item = pref.getSelectedUserLocation()
        if(item!=null){
            emptyFavRoute.visibility = View.GONE
            infoLayout.visibility = View.VISIBLE
            locationA.text = item.startLocation.locationName
            locationB.text = item.endLocation.locationName
            distance.text = item.distance.toString()
        }
    }
}