package com.example.weather.ui.models

import org.json.JSONArray
import org.json.JSONObject

data class Day(
    val cloudcover: Double,
    val conditions: String,
    val datetime: String,
    val datetimeEpoch: Int,
    val description: String,
    val dew: Double,
    val feelslike: Double,
    val feelslikemax: Double,
    val feelslikemin: Double,
    val hours: List<Hour>,
    val humidity: Double,
    val icon: String,
    val moonphase: Double,
    val precip: Double,
    val precipcover: Double,
    val precipprob: Int,
    val preciptype: List<String>,
    val pressure: Double,
    val severerisk: Int,
    val snow: Int,
    val snowdepth: Int,
    val solarenergy: Double,
    val solarradiation: Double,
    val source: String,
    val stations: List<String>,
    val sunrise: String,
    val sunriseEpoch: Int,
    val sunset: String,
    val sunsetEpoch: Int,
    val temp: Double,
    val tempmax: Double,
    val tempmin: Double,
    val uvindex: Int,
    val visibility: Double,
    val winddir: Double,
    val windgust: Double,
    val windspeed: Double,
) {
    companion object {
        fun fromJson(json: JSONObject): Day {
            val hoursArray = json.optJSONArray("hours") ?: JSONArray()
            val hoursList = mutableListOf<Hour>()

            for (i in 0 until hoursArray.length()) {
                val hourObject = hoursArray.getJSONObject(i)
                hoursList.add(Hour.fromJson(hourObject))
            }

            val precipTypeArray = json.optJSONArray("preciptype")
            val precipTypeList = mutableListOf<String>()

            if (precipTypeArray != null) {
                for (i in 0 until precipTypeArray.length()) {
                    precipTypeList.add(precipTypeArray.optString(i, ""))
                }
            }

            val stationsArray = json.optJSONArray("stations")
            val stationsList = mutableListOf<String>()

            if (stationsArray != null) {
                for (i in 0 until stationsArray.length()) {
                    stationsList.add(stationsArray.optString(i, ""))
                }
            }

            return Day(
                cloudcover = json.optDouble("cloudcover", 0.0),
                conditions = json.optString("conditions", ""),
                datetime = json.optString("datetime", ""),
                datetimeEpoch = json.optInt("datetimeEpoch", 0),
                description = json.optString("description", ""),
                dew = json.optDouble("dew", 0.0),
                feelslike = json.optDouble("feelslike", 0.0),
                feelslikemax = json.optDouble("feelslikemax", 0.0),
                feelslikemin = json.optDouble("feelslikemin", 0.0),
                hours = hoursList,
                humidity = json.optDouble("humidity", 0.0),
                icon = json.optString("icon", ""),
                moonphase = json.optDouble("moonphase", 0.0),
                precip = json.optDouble("precip", 0.0),
                precipcover = json.optDouble("precipcover", 0.0),
                precipprob = json.optInt("precipprob", 0),
                preciptype = precipTypeList,
                pressure = json.optDouble("pressure", 0.0),
                severerisk = json.optInt("severerisk", 0),
                snow = json.optInt("snow", 0),
                snowdepth = json.optInt("snowdepth", 0),
                solarenergy = json.optDouble("solarenergy", 0.0),
                solarradiation = json.optDouble("solarradiation", 0.0),
                source = json.optString("source", ""),
                stations = stationsList,
                sunrise = json.optString("sunrise", ""),
                sunriseEpoch = json.optInt("sunriseEpoch", 0),
                sunset = json.optString("sunset", ""),
                sunsetEpoch = json.optInt("sunsetEpoch", 0),
                temp = json.optDouble("temp", 0.0),
                tempmax = json.optDouble("tempmax", 0.0),
                tempmin = json.optDouble("tempmin", 0.0),
                uvindex = json.optInt("uvindex", 0),
                visibility = json.optDouble("visibility", 0.0),
                winddir = json.optDouble("winddir", 0.0),
                windgust = json.optDouble("windgust", 0.0),
                windspeed = json.optDouble("windspeed", 0.0)
            )
        }
    }
}