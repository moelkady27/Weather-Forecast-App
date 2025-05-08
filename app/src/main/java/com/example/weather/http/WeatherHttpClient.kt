package com.example.weather.http

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


object WeatherHttpClient {
    private const val TAG = "WeatherHttpClient"
    private const val BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
    private const val API_KEY = "YH5A9E2FQYH4F4KYLU6WQA6LQ"
    private const val CONNECT_TIMEOUT = 15000
    private const val READ_TIMEOUT = 15000
    private val executor = Executors.newCachedThreadPool()

    interface WeatherCallback {
        fun onSuccess(response: JSONObject)
        fun onError(errorMessage: String)
    }

    fun getWeather(location: String, date: String = "today", callback: WeatherCallback) {
        executor.execute {
            try {
                val urlString = "$BASE_URL$location/$date?unitGroup=uk&key=$API_KEY&contentType=json"
                val connection = URL(urlString).openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = CONNECT_TIMEOUT
                    connection.readTimeout = READ_TIMEOUT
                    connection.setRequestProperty("Content-Type", "application/json")

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = readStream(connection)
                        try {
                            val jsonResponse = JSONObject(response)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                callback.onSuccess(jsonResponse)
                            }
                        } catch (e: JSONException) {
                            handleError("JSON parsing error: ${e.message}", callback)
                        }
                    } else {
                        val errorMessage = "HTTP error: $responseCode"
                        handleError(errorMessage, callback)
                    }
                } catch (e: Exception) {
                    handleError("Connection error: ${e.message}", callback)
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                handleError("Network error: ${e.message}", callback)
            }
        }
    }

    private fun handleError(message: String, callback: WeatherCallback) {
        Log.e(TAG, message)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            callback.onError(message)
        }
    }

    private fun readStream(connection: HttpURLConnection): String {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        return response.toString()
    }
}