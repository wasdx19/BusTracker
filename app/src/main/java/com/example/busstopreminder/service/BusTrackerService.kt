package com.example.busstopreminder.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.busstopreminder.R
import com.example.busstopreminder.presentation.main.MainActivity
import com.example.busstopreminder.presentation.notificationActivity.NotificationActivity


class BusTrackerService : Service() {

    lateinit var notifManager: NotificationManager

    private var contentText = "Ваш автобус приближается"
    private var subText = "Автобус в скором времени прибудет"

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Напоминалка активирована", Toast.LENGTH_SHORT).show()
        val contentTxt = intent?.getStringExtra("contentText").orEmpty()
        val subTxt = intent?.getStringExtra("subText").orEmpty()
        try{
            Handler().postDelayed({
                if(contentTxt.isEmpty() && subTxt.isEmpty()) {
                    sendNotification(contentText, subText)
                }else{
                    sendNotification(contentTxt, subTxt)
                }
            }, 20000)
        } catch (e: InterruptedException){
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendNotification(content: String, subText: String) {
        val intent = Intent(this, NotificationActivity()::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this)

        builder.setAutoCancel(false)
        builder.setTicker("BuStop Reminder")
        builder.setContentTitle("Уведомление от BuStop Reminder")
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setContentIntent(pIntent)
        builder.setOngoing(true)
        builder.setSubText(subText) //API level 16
        builder.setVibrate(longArrayOf( 1000, 3000, 1000, 3000, 1000, 3000, 1000 ))

        builder.setNumber(100)
        builder.build()

        val notif = builder.notification
        notifManager.notify(11, notif)
    }
}