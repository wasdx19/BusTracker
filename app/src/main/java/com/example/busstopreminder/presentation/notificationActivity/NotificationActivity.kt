package com.example.busstopreminder.presentation.notificationActivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.busstopreminder.R
import com.example.busstopreminder.service.BusTrackerService
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity: AppCompatActivity(R.layout.activity_notification) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnclickListener()
    }

    private fun setOnclickListener() {
        takePlacedButton.setOnClickListener {
            val intent = Intent(this, BusTrackerService::class.java)
            intent.putExtra("contentText", "Мы приближаемся к последней точке")
            intent.putExtra("subText", "Хорошего дня)")
            startService(intent)
            finish()
        }
        forgotBusButton.setOnClickListener {
            val intent = Intent(this, BusTrackerService::class.java)
            intent.putExtra("contentText", "Ваш автобус приближается")
            intent.putExtra("subText", "Автобус в скором времени прибудет")
            startService(intent)
            finish()
        }
        exit.setOnClickListener {
            finish()
        }
    }
}