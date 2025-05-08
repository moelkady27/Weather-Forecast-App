package com.example.weather.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.http.WeatherHttpClient
import com.example.weather.network.NetworkUtils
import com.example.weather.storage.AppReferences
import com.example.weather.storage.BaseActivity
import com.example.weather.ui.models.Day
import com.example.weather.ui.models.GetCurrentWeatherResponse
import com.example.weather.ui.models.Hour
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

private const val LOCATION_PERMISSION_REQUEST_CODE = 101

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var networkUtils: NetworkUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppReferences.init(applicationContext)

        swipeRefreshLayout = binding.swipeRefresh
        networkUtils = NetworkUtils(this@MainActivity)

        setupNetworkMonitoring()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val dateFormat = SimpleDateFormat("MMMM, dd", Locale.getDefault())
        binding.dateLabel.text = dateFormat.format(Date())

        swipeRefreshLayout.setOnRefreshListener {
            refreshWeatherData()
        }

        refreshWeatherData()

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Your location provider is turned off. Please turn it on.", Toast.LENGTH_SHORT).show()
            loadCachedWeatherData()
        } else {
            checkLocationPermissions()
        }

        binding.nextWeather.setOnClickListener {
            startActivity(Intent(this@MainActivity, NextWeatherActivity::class.java))
        }
    }

    private fun setupNetworkMonitoring() {
        networkUtils.startNetworkMonitoring(object : NetworkUtils.NetworkStateListener {
            override fun onNetworkAvailable() {
                runOnUiThread {
                    hideOfflineIndicator()
                    Toast.makeText(this@MainActivity, "Network connection restored", Toast.LENGTH_SHORT).show()
                    refreshWeatherData()
                }
            }

            override fun onNetworkLost() {
                runOnUiThread {
                    showOfflineErrorMessage()
                    loadCachedWeatherData()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        networkUtils.stopNetworkMonitoring()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationData()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            loadCachedWeatherData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationData()
        } else {
            Toast.makeText(
                this,
                "Location permissions are required to get your current location.",
                Toast.LENGTH_SHORT
            ).show()
            loadCachedWeatherData()
        }
    }

    private fun updateHour(day: Day?) {
        day?.let {
            val targetHours = listOf("15:00:00", "16:00:00", "17:00:00", "18:00:00")
            val forecastHours = it.hours.filter { hour ->
                targetHours.contains(hour.datetime)
            }.sortedBy { hour ->
                targetHours.indexOf(hour.datetime)
            }

            if (forecastHours.size == 4) {
                binding.hour1Temp.text = "${forecastHours[0].temp.toInt()}°C"
                binding.hour2Temp.text = "${forecastHours[1].temp.toInt()}°C"
                binding.hour3Temp.text = "${forecastHours[2].temp.toInt()}°C"
                binding.hour4Temp.text = "${forecastHours[3].temp.toInt()}°C"

                binding.hour1.text = formatHourDisplay(forecastHours[0].datetime)
                binding.hour2.text = formatHourDisplay(forecastHours[1].datetime)
                binding.hour3.text = formatHourDisplay(forecastHours[2].datetime)
                binding.hour4.text = formatHourDisplay(forecastHours[3].datetime)
            }
        }
    }

    fun formatHourDisplay(time: String): String {
        val hour = time.substringBefore(":")
        return "$hour.00"
    }

    private fun requestLocationData() {
        Log.e("Location", "Requesting location data...")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            loadCachedWeatherData()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                reverseGeocodeLocation(latitude, longitude)

                val locationString = "$latitude,$longitude"
                getCurrentWeather(locationString)
            } else {
                Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show()
                loadCachedWeatherData()
            }
        }
    }

    private fun reverseGeocodeLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())

        Executors.newSingleThreadExecutor().execute {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    val cityName = address.locality
                    val countryName = address.countryName

                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error: ${e.localizedMessage}")
            }
        }
    }

    private fun getCurrentWeather(location: String) {
        if (!networkUtils.isNetworkAvailable()) {
            Log.e("Weather", "No network connection")

            showOfflineErrorMessage()

            if (!loadCachedWeatherData()) {
                binding.weatherDescription.text = "No weather data available"
                binding.temperatureText.text = "--°"
                binding.minMaxTemp.text = "Please connect to the internet"
            }
            return
        }

        showProgressDialog(this@MainActivity, "Loading weather data...")

        WeatherHttpClient.getWeather(location, "today", object : WeatherHttpClient.WeatherCallback {
            override fun onSuccess(response: JSONObject) {
                hideProgressDialog()
                swipeRefreshLayout.isRefreshing = false

                hideOfflineIndicator()

                try {
                    AppReferences.saveWeatherData(response, location)
                    Log.e("Weather", "Weather data cached successfully")

                    updateUIWithWeatherData(response)
                } catch (e: Exception) {
                    Log.e("WeatherParsing", "Error parsing weather data: ${e.message}")
                    Toast.makeText(this@MainActivity, "Error parsing weather data", Toast.LENGTH_SHORT).show()
                    loadCachedWeatherData()
                }
            }

            override fun onError(errorMessage: String) {
                hideProgressDialog()
                swipeRefreshLayout.isRefreshing = false
                Log.e("MainActivity", "Error fetching weather: $errorMessage")
                Toast.makeText(this@MainActivity, "Failed to load weather data: $errorMessage", Toast.LENGTH_SHORT).show()

                if (!loadCachedWeatherData()) {
                    binding.weatherDescription.text = "Error fetching data"
                    binding.temperatureText.text = "--°"
                    binding.minMaxTemp.text = errorMessage
                }
            }
        })
    }

    private fun loadCachedWeatherData(): Boolean {
        if (AppReferences.hasCache()) {
            val cachedData = AppReferences.getLastWeatherData()
            if (cachedData != null) {
                Log.i("Weather", "Loading cached weather data")
                updateUIWithWeatherData(cachedData)

                val lastUpdateTime = Date(AppReferences.getLastUpdateTime())
                val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                val formattedTime = dateFormat.format(lastUpdateTime)

                showOfflineWithCachedDataIndicator(formattedTime)

                return true
            }
        }

        if (!swipeRefreshLayout.isRefreshing) {
            Toast.makeText(this, "No cached weather data available", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    private fun showOfflineErrorMessage() {
        Toast.makeText(this@MainActivity, "You are offline.", Toast.LENGTH_LONG).show()

        showOfflineIndicator()
    }

    private fun showOfflineIndicator() {
        val offlineLayout = findViewById<LinearLayout>(R.id.offline_status_layout)
        offlineLayout?.visibility = android.view.View.VISIBLE

        val offlineText = findViewById<TextView>(R.id.offline_status_text)
        offlineText?.text = "You are offline. Weather data cannot be updated."

        binding.weatherDescription.text = binding.weatherDescription.text.toString() + " (OFFLINE)"
    }

    private fun showOfflineWithCachedDataIndicator(timestamp: String) {
        val offlineLayout = findViewById<LinearLayout>(R.id.offline_status_layout)
        offlineLayout?.visibility = android.view.View.VISIBLE

        val offlineText = findViewById<TextView>(R.id.offline_status_text)
        offlineText?.text = "Offline mode: Showing cached weather data from $timestamp"

        Toast.makeText(this@MainActivity, "Showing cached weather data from $timestamp", Toast.LENGTH_LONG).show()

        val currentDesc = binding.weatherDescription.text.toString()
        if (!currentDesc.contains("(CACHED)")) {
            binding.weatherDescription.text = "$currentDesc (CACHED)"
        }
    }

    private fun hideOfflineIndicator() {
        val offlineLayout = findViewById<LinearLayout>(R.id.offline_status_layout)
        offlineLayout?.visibility = android.view.View.GONE

        val currentDesc = binding.weatherDescription.text.toString()
        binding.weatherDescription.text = currentDesc
            .replace(" (OFFLINE)", "")
            .replace(" (CACHED)", "")
    }

    private fun updateUIWithWeatherData(weatherData: JSONObject) {
        try {
            val weatherResponse = GetCurrentWeatherResponse.fromJson(weatherData)

            val currentHour = getCurrentHourTime()
            Log.e("Current Time", "Current Time: $currentHour")

            val today: Day? = weatherResponse.days.firstOrNull()
            val hourData: Hour? = today?.hours?.firstOrNull { hour -> hour.datetime == currentHour }

            hourData?.let { h ->
                binding.temperatureText.text = "${h.temp.toInt()}°"
                binding.weatherDescription.text = h.conditions
                binding.minMaxTemp.text = "Max: ${today.tempmax.toInt()}° Min:${today.tempmin.toInt()}°"

                Log.e("Weather", "Temp: ${h.temp}, Condition: ${h.conditions}")

                updateHour(today)
            }
        } catch (e: Exception) {
            Log.e("WeatherParsing", "Error parsing weather data: ${e.message}")
            Toast.makeText(this@MainActivity, "Error parsing weather data", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentHourTime(): String {
        val formatter = SimpleDateFormat("HH:00:00", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun refreshWeatherData() {
        swipeRefreshLayout.isRefreshing = true

        if (!networkUtils.isNetworkAvailable()) {
            showOfflineErrorMessage()
            loadCachedWeatherData()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                hideProgressDialog()
                if (!isFinishing && !isDestroyed) {
                    requestLocationData()
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }, 2500)
    }
}