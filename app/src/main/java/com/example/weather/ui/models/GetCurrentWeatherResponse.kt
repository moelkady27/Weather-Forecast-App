package com.example.weather.ui.models

import org.json.JSONObject

data class GetCurrentWeatherResponse(
    val address: String,
    val alerts: List<Any>,
    val currentConditions: CurrentConditions,
    val days: List<Day>,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val queryCost: Int,
    val resolvedAddress: String,
    val stations: HEBA,
    val timezone: String,
    val tzoffset: Int
) {
    companion object {
        fun fromJson(json: JSONObject): GetCurrentWeatherResponse {
            val daysArray = json.getJSONArray("days")
            val daysList = mutableListOf<Day>()

            for (i in 0 until daysArray.length()) {
                val dayObject = daysArray.getJSONObject(i)
                daysList.add(Day.fromJson(dayObject))
            }

            val currentConditionsObj = json.optJSONObject("currentConditions")
            val currentConditions = if (currentConditionsObj != null) {
                CurrentConditions.fromJson(currentConditionsObj)
            } else {
                CurrentConditions.createDefault()
            }

            val stationsObj = json.optJSONObject("stations") ?: JSONObject()
            val stations = Stations.fromJson(stationsObj)

            val alertsList = mutableListOf<Any>()

            return GetCurrentWeatherResponse(
                address = json.optString("address", ""),
                alerts = alertsList,
                currentConditions = currentConditions,
                days = daysList,
                description = json.optString("description", ""),
                latitude = json.optDouble("latitude", 0.0),
                longitude = json.optDouble("longitude", 0.0),
                queryCost = json.optInt("queryCost", 0),
                resolvedAddress = json.optString("resolvedAddress", ""),
                stations = stations,
                timezone = json.optString("timezone", ""),
                tzoffset = json.optInt("tzoffset", 0)
            )
        }
    }
}