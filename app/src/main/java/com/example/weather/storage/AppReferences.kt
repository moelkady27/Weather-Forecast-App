package com.example.weather.storage

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object AppReferences {
    private const val PREF_NAME = "WeatherAppPreferences"
    private const val KEY_WEATHER_DATA = "weather_data"
    private const val KEY_LAST_LOCATION = "last_location"
    private const val KEY_LAST_UPDATE_TIME = "last_update_time"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveWeatherData(weatherData: JSONObject, location: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_WEATHER_DATA, weatherData.toString())
        editor.putString(KEY_LAST_LOCATION, location)
        editor.putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis())
        editor.apply()
    }

    fun getLastWeatherData(): JSONObject? {
        val weatherDataString = sharedPreferences.getString(KEY_WEATHER_DATA, null)
        return if (weatherDataString != null) {
            try {
                JSONObject(weatherDataString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun getLastLocation(): String {
        return sharedPreferences.getString(KEY_LAST_LOCATION, "") ?: ""
    }

    fun getLastUpdateTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_UPDATE_TIME, 0)
    }

    fun isDataFresh(): Boolean {
        val lastUpdateTime = getLastUpdateTime()
        if (lastUpdateTime == 0L) return false

        val oneHourInMillis = 60 * 60 * 1000
        return System.currentTimeMillis() - lastUpdateTime < oneHourInMillis
    }

    fun hasCache(): Boolean {
        return getLastWeatherData() != null && getLastLocation().isNotEmpty()
    }

    fun clearCache() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_WEATHER_DATA)
        editor.remove(KEY_LAST_LOCATION)
        editor.remove(KEY_LAST_UPDATE_TIME)
        editor.apply()
    }
}