package com.example.busstopreminder

import android.app.Application
import com.example.busstopreminder.common.constants.Constants
import com.example.busstopreminder.di.appModule
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(Constants.MAPKIT_API_KEY)

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}