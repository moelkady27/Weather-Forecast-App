package com.example.weather.ui.models

import org.json.JSONObject

data class Hour(
    val cloudcover: Double,
    val conditions: String,
    val datetime: String,
    val datetimeEpoch: Int,
    val dew: Double,
    val feelslike: Double,
    val humidity: Double,
    val icon: String,
    val precip: Double,
    val precipprob: Int,
    val preciptype: List<String>?,
    val pressure: Double,
    val severerisk: Int,
    val snow: Int,
    val snowdepth: Int,
    val solarenergy: Double,
    val solarradiation: Int,
    val source: String,
    val stations: List<String>,
    val temp: Double,
    val uvindex: Int,
    val visibility: Double,
    val winddir: Double,
    val windgust: Double,
    val windspeed: Double
) {
    companion object {
        fun fromJson(json: JSONObject): Hour {
            val precipTypeArray = json.optJSONArray("preciptype")
            val precipTypeList: List<String>? = if (precipTypeArray != null) {
                val list = mutableListOf<String>()
                for (i in 0 until precipTypeArray.length()) {
                    list.add(precipTypeArray.optString(i, ""))
                }
                list
            } else {
                null
            }

            val stationsArray = json.optJSONArray("stations")
            val stationsList = mutableListOf<String>()

            if (stationsArray != null) {
                for (i in 0 until stationsArray.length()) {
                    stationsList.add(stationsArray.optString(i, ""))
                }
            }

            return Hour(
                cloudcover = json.optDouble("cloudcover", 0.0),
                conditions = json.optString("conditions", ""),
                datetime = json.optString("datetime", ""),
                datetimeEpoch = json.optInt("datetimeEpoch", 0),
                dew = json.optDouble("dew", 0.0),
                feelslike = json.optDouble("feelslike", 0.0),
                humidity = json.optDouble("humidity", 0.0),
                icon = json.optString("icon", ""),
                precip = json.optDouble("precip", 0.0),
                precipprob = json.optInt("precipprob", 0),
                preciptype = precipTypeList,
                pressure = json.optDouble("pressure", 0.0),
                severerisk = json.optInt("severerisk", 0),
                snow = json.optInt("snow", 0),
                snowdepth = json.optInt("snowdepth", 0),
                solarenergy = json.optDouble("solarenergy", 0.0),
                solarradiation = json.optInt("solarradiation", 0),
                source = json.optString("source", ""),
                stations = stationsList,
                temp = json.optDouble("temp", 0.0),
                uvindex = json.optInt("uvindex", 0),
                visibility = json.optDouble("visibility", 0.0),
                winddir = json.optDouble("winddir", 0.0),
                windgust = json.optDouble("windgust", 0.0),
                windspeed = json.optDouble("windspeed", 0.0)
            )
        }
    }
}