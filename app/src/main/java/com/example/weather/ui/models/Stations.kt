package com.example.weather.ui.models

import org.json.JSONObject

data class Stations(
    val HEBA: HEBA
) {
    companion object {
        fun fromJson(json: JSONObject): HEBA {
            return HEBA(
                distance = json.optDouble("distance", 0.0),
                latitude = json.optDouble("latitude", 0.0),
                longitude = json.optDouble("longitude", 0.0),
                useCount = json.optInt("useCount", 0),
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                quality = json.optInt("quality", 0),
                contribution = json.optDouble("contribution", 0.0)
            )
        }
    }
}