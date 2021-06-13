package com.example.busstopreminder.di

import com.example.busstopreminder.common.pref.Preference
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { Preference(androidContext()) }
}