package com.example.busstopreminder.presentation.launcher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.busstopreminder.R
import com.example.busstopreminder.presentation.actions.ActionsActivity

class LauncherActivity: AppCompatActivity(R.layout.activity_launcher){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            val intent = Intent(this, ActionsActivity::class.java)
            startActivity(intent)
            finish()
        },3000)
    }
}