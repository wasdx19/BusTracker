package com.example.busstopreminder.common.pref

import android.content.Context
import android.content.SharedPreferences
import com.example.busstopreminder.data.route.RoutesDTO
import com.example.busstopreminder.data.userLocations.UserLocationItemDTO
import com.example.busstopreminder.data.userLocations.UserLocationsDTO
import com.google.gson.Gson

class Preference(context: Context) {

    companion object{
        const val PREF_NAME = "BUS_TRACKER_PREF"
        const val ROUTES = "ROUTES"
        const val USER_LOCATIONS = "USER_LOCATIONS"
        const val USER_SELECTED_LOCATION = "USER_SELECTED_LOCATION"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    fun saveUserLocation(data: UserLocationsDTO){
        val json = gson.toJson(data)
        editor.putString(USER_LOCATIONS, json)
        editor.apply()
    }

    fun getUserLocation(): UserLocationsDTO? {
        val json = sharedPreferences.getString(USER_LOCATIONS, "")
        val obj = gson.fromJson(json, UserLocationsDTO::class.java)
        return obj
    }

    fun setSelectedUserLocation(data: UserLocationItemDTO){
        val json = gson.toJson(data)
        editor.putString(USER_SELECTED_LOCATION, json)
        editor.apply()
    }

    fun getSelectedUserLocation() : UserLocationItemDTO? {
        val json = sharedPreferences.getString(USER_SELECTED_LOCATION, "")
        val obj = gson.fromJson(json, UserLocationItemDTO::class.java)
        return obj
    }

    fun setRoutes(data: RoutesDTO){
        val json = gson.toJson(data)
        editor.putString(ROUTES, json)
        editor.apply()
    }

    fun getRoutes(): RoutesDTO?{
        val json = sharedPreferences.getString(ROUTES, "")
        val obj = gson.fromJson(json, RoutesDTO::class.java)
        return obj
    }

}