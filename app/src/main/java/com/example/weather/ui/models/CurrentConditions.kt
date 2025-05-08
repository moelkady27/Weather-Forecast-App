package com.example.weather.ui.models

import org.json.JSONObject

data class CurrentConditions(
    val cloudcover: Double,
    val conditions: String,
    val datetime: String,
    val datetimeEpoch: Int,
    val dew: Double,
    val feelslike: Double,
    val humidity: Double,
    val icon: String,
    val moonphase: Double,
    val precip: Any,
    val precipprob: Int,
    val preciptype: Any?,
    val pressure: Int,
    val snow: Int,
    val snowdepth: Int,
    val solarenergy: Double,
    val solarradiation: Int,
    val source: String,
    val stations: List<String>,
    val sunrise: String,
    val sunriseEpoch: Int,
    val sunset: String,
    val sunsetEpoch: Int,
    val temp: Double,
    val uvindex: Int,
    val visibility: Double,
    val winddir: Int,
    val windgust: Any?,
    val windspeed: Double
) {
    companion object {
        fun fromJson(json: JSONObject): CurrentConditions {
            val stationsList = mutableListOf<String>()
            val stationsArray = json.optJSONArray("stations")
            if (stationsArray != null) {
                for (i in 0 until stationsArray.length()) {
                    stationsList.add(stationsArray.optString(i, ""))
                }
            }

            return CurrentConditions(
                cloudcover = json.optDouble("cloudcover", 0.0),
                conditions = json.optString("conditions", ""),
                datetime = json.optString("datetime", ""),
                datetimeEpoch = json.optInt("datetimeEpoch", 0),
                dew = json.optDouble("dew", 0.0),
                feelslike = json.optDouble("feelslike", 0.0),
                humidity = json.optDouble("humidity", 0.0),
                icon = json.optString("icon", ""),
                moonphase = json.optDouble("moonphase", 0.0),
                precip = json.opt("precip") ?: 0.0,
                precipprob = json.optInt("precipprob", 0),
                preciptype = json.opt("preciptype") ?: emptyList<String>(),
                pressure = json.optInt("pressure", 0),
                snow = json.optInt("snow", 0),
                snowdepth = json.optInt("snowdepth", 0),
                solarenergy = json.optDouble("solarenergy", 0.0),
                solarradiation = json.optInt("solarradiation", 0),
                source = json.optString("source", ""),
                stations = stationsList,
                sunrise = json.optString("sunrise", ""),
                sunriseEpoch = json.optInt("sunriseEpoch", 0),
                sunset = json.optString("sunset", ""),
                sunsetEpoch = json.optInt("sunsetEpoch", 0),
                temp = json.optDouble("temp", 0.0),
                uvindex = json.optInt("uvindex", 0),
                visibility = json.optDouble("visibility", 0.0),
                winddir = json.optInt("winddir", 0),
                windgust = json.opt("windgust") ?: 0.0,
                windspeed = json.optDouble("windspeed", 0.0)
            )
        }

        fun createDefault(): CurrentConditions {
            return CurrentConditions(
                cloudcover = 0.0,
                conditions = "",
                datetime = "",
                datetimeEpoch = 0,
                dew = 0.0,
                feelslike = 0.0,
                humidity = 0.0,
                icon = "",
                moonphase = 0.0,
                precip = 0.0,
                precipprob = 0,
                preciptype = null,
                pressure = 0,
                snow = 0,
                snowdepth = 0,
                solarenergy = 0.0,
                solarradiation = 0,
                source = "",
                stations = emptyList(),
                sunrise = "",
                sunriseEpoch = 0,
                sunset = "",
                sunsetEpoch = 0,
                temp = 0.0,
                uvindex = 0,
                visibility = 0.0,
                winddir = 0,
                windgust = null,
                windspeed = 0.0
            )
        }
    }
}