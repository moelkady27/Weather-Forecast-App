package com.example.weather.ui.models

import org.json.JSONObject

data class HEBA(
    val contribution: Double,
    val distance: Double,
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val quality: Int,
    val useCount: Int
) {
        companion object {
        fun fromJson(json: JSONObject): HEBA {
            return HEBA(
                contribution = json.optDouble("contribution", 0.0),
                distance = json.optDouble("distance", 0.0),
                id = json.optString("id", ""),
                latitude = json.optDouble("latitude", 0.0),
                longitude = json.optDouble("longitude", 0.0),
                name = json.optString("name", ""),
                quality = json.optInt("quality", 0),
                useCount = json.optInt("useCount", 0)
            )
        }
    }
}