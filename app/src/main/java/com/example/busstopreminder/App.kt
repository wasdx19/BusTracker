package com.example.busstopreminder

import android.app.Application
import com.example.busstopreminder.common.constants.Constants
import com.yandex.mapkit.MapKitFactory

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(Constants.MAPKIT_API_KEY)
    }
}