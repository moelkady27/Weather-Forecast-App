package com.example.weather.ui.activities

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.weather.R
import com.example.weather.databinding.ActivityNextWeatherBinding
import com.example.weather.http.WeatherHttpClient
import com.example.weather.network.NetworkUtils
import com.example.weather.storage.BaseActivity
import com.example.weather.ui.models.Day
import com.example.weather.ui.models.GetCurrentWeatherResponse
import com.example.weather.ui.models.Hour
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NextWeatherActivity : BaseActivity() {

    private lateinit var binding: ActivityNextWeatherBinding
    private lateinit var networkUtils: NetworkUtils

    private val demoLocation = "31.0345,30.4605"
    private var selectedDayIndex = 0
    private var weatherDays: List<Day> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNextWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkUtils = NetworkUtils(this@NextWeatherActivity)

        selectedDayIndex = intent.getIntExtra("SELECTED_DAY_INDEX", 0)

        setupSwipeRefresh()
        loadWeatherForecast()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadWeatherForecast()
        }
    }

    private fun loadWeatherForecast() {
        showProgressDialog(this, "Loading forecast data...")
        binding.swipeRefresh.isRefreshing = true

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 5)
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val dateRange = "$today/$endDate"

        WeatherHttpClient.getWeather(demoLocation, dateRange, object : WeatherHttpClient.WeatherCallback {
            override fun onSuccess(response: JSONObject) {
                hideProgressDialog()
                binding.swipeRefresh.isRefreshing = false

                try {
                    val weatherResponse = GetCurrentWeatherResponse.fromJson(response)
                    Log.e("NextWeatherActivity", "Resolved Address: ${weatherResponse.resolvedAddress}")

                    weatherDays = weatherResponse.days
                    updateUI(weatherResponse)

                    updateSelectedDayUI()

                    val latLng = demoLocation.split(",")
                    val lat = latLng[0].toDouble()
                    val lng = latLng[1].toDouble()

                    reverseGeocodeLocation(lat, lng) { locationName ->
                        binding.locationText.text = locationName
                    }

                } catch (e: Exception) {
                    Log.e("NextWeatherActivity", "Error parsing weather data: ${e.message}")
                    Toast.makeText(this@NextWeatherActivity, "Error parsing weather data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(errorMessage: String) {
                hideProgressDialog()
                binding.swipeRefresh.isRefreshing = false
                Log.e("NextWeatherActivity", "Error fetching weather: $errorMessage")
                Toast.makeText(this@NextWeatherActivity, "Failed to load weather data: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateSelectedDayUI() {
        if (weatherDays.isEmpty() || selectedDayIndex >= weatherDays.size) return

        val selectedDay = weatherDays[selectedDayIndex]
        val isCurrentDay = isSelectedDayToday()

        if (isCurrentDay) {
            val currentHour = getCurrentHourTime()
            val hourData = selectedDay.hours.find { it.datetime == currentHour }

            if (hourData != null) {
                updateCurrentHourData(hourData, selectedDay)
            } else {
                updateDayData(selectedDay)
            }
        } else {
            updateDayData(selectedDay)
        }

        binding.airQualityValue.text = "${selectedDay.windspeed.toInt()} km/h"
        binding.sunriseTime.text = formatTime(selectedDay.sunrise)
        binding.sunsetTime.text = "Sunset: ${formatTime(selectedDay.sunset)}"
        binding.uvIndexValue.text = selectedDay.uvindex.toString()
        binding.uvIndexDescription.text = getUvIndexDescription(selectedDay.uvindex)

        highlightSelectedDayCard()
    }

    private fun updateCurrentHourData(hourData: Hour, day: Day) {
        binding.temperatureRange.text = "${hourData.temp.toInt()}°C (Current)"

        binding.temperatureRange.append(" | Max: ${day.tempmax.toInt()}°   Min:${day.tempmin.toInt()}°")
    }

    private fun updateDayData(day: Day) {
        binding.temperatureRange.text = "Max: ${day.tempmax.toInt()}°   Min:${day.tempmin.toInt()}°"
    }

    private fun isSelectedDayToday(): Boolean {
        if (selectedDayIndex != 0) return false

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val selectedDayDate = weatherDays[selectedDayIndex].datetime

        return today == selectedDayDate
    }

    private fun getCurrentHourTime(): String {
        val formatter = SimpleDateFormat("HH:00:00", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun highlightSelectedDayCard() {
        binding.mondayCard.setCardBackgroundColor(resources.getColor(R.color.card_default_color, null))
        binding.tuesdayCard.setCardBackgroundColor(resources.getColor(R.color.card_default_color, null))
        binding.wednesdayCard.setCardBackgroundColor(resources.getColor(R.color.card_default_color, null))
        binding.thursdayCard.setCardBackgroundColor(resources.getColor(R.color.card_default_color, null))
        binding.fridayCard.setCardBackgroundColor(resources.getColor(R.color.card_default_color, null))

        when (selectedDayIndex) {
            0 -> binding.mondayCard.setCardBackgroundColor(resources.getColor(R.color.card_selected_color, null))
            1 -> binding.tuesdayCard.setCardBackgroundColor(resources.getColor(R.color.card_selected_color, null))
            2 -> binding.wednesdayCard.setCardBackgroundColor(resources.getColor(R.color.card_selected_color, null))
            3 -> binding.thursdayCard.setCardBackgroundColor(resources.getColor(R.color.card_selected_color, null))
            4 -> binding.fridayCard.setCardBackgroundColor(resources.getColor(R.color.card_selected_color, null))
        }
    }

    private fun updateUI(weatherResponse: GetCurrentWeatherResponse) {
        binding.locationText.text = weatherResponse.resolvedAddress

        updateDailyForecast(weatherResponse.days)
    }

    private fun updateDailyForecast(days: List<Day>) {
        if (days.size < 4) return

        binding.mondayCard.apply {
            findViewById<android.widget.TextView>(R.id.dayTemp1).text = "${days[0].temp.toInt()}°C"
            findViewById<android.widget.TextView>(R.id.dayName1).text = getDayOfWeek(0)
        }

        binding.tuesdayCard.apply {
            findViewById<android.widget.TextView>(R.id.dayTemp2).text = "${days[1].temp.toInt()}°C"
            findViewById<android.widget.TextView>(R.id.dayName2).text = getDayOfWeek(1)
        }

        binding.wednesdayCard.apply {
            findViewById<android.widget.TextView>(R.id.dayTemp3).text = "${days[2].temp.toInt()}°C"
            findViewById<android.widget.TextView>(R.id.dayName3).text = getDayOfWeek(2)
        }

        binding.thursdayCard.apply {
            findViewById<android.widget.TextView>(R.id.dayTemp4).text = "${days[3].temp.toInt()}°C"
            findViewById<android.widget.TextView>(R.id.dayName4).text = getDayOfWeek(3)
        }

        binding.fridayCard.apply {
            findViewById<android.widget.TextView>(R.id.dayTemp5).text = "${days[4].temp.toInt()}°C"
            findViewById<android.widget.TextView>(R.id.dayName5).text = getDayOfWeek(4)
        }

        setupDayCardClickListeners()
    }

    private fun setupDayCardClickListeners() {
        binding.mondayCard.setOnClickListener {
            selectedDayIndex = 0
            updateSelectedDayUI()
        }

        binding.tuesdayCard.setOnClickListener {
            selectedDayIndex = 1
            updateSelectedDayUI()
        }

        binding.wednesdayCard.setOnClickListener {
            selectedDayIndex = 2
            updateSelectedDayUI()
        }

        binding.thursdayCard.setOnClickListener {
            selectedDayIndex = 3
            updateSelectedDayUI()
        }

        binding.fridayCard.setOnClickListener {
            selectedDayIndex = 4
            updateSelectedDayUI()
        }
    }

    private fun getDayOfWeek(daysFromToday: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysFromToday)
        return SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
    }

    private fun formatTime(timeString: String): String {
        try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(timeString) ?: return timeString
            return outputFormat.format(date)
        } catch (e: Exception) {
            return timeString
        }
    }

    private fun getUvIndexDescription(uvIndex: Int): String {
        return when {
            uvIndex <= 2 -> "Low"
            uvIndex <= 5 -> "Moderate"
            uvIndex <= 7 -> "High"
            uvIndex <= 10 -> "Very High"
            else -> "Extreme"
        }
    }

    private fun reverseGeocodeLocation(latitude: Double, longitude: Double, onResult: (String) -> Unit) {
        val geocoder = Geocoder(this@NextWeatherActivity, Locale.getDefault())
        Thread {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val locationName = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality
                    val country = address.countryName
                    when {
                        !city.isNullOrEmpty() && !country.isNullOrEmpty() -> "$city, $country"
                        !city.isNullOrEmpty() -> city
                        !country.isNullOrEmpty() -> country
                        else -> "Unknown location"
                    }
                } else {
                    "Unknown location"
                }

                runOnUiThread {
                    onResult(locationName)
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Failed to reverse geocode: ${e.localizedMessage}")
                runOnUiThread {
                    onResult("Unknown location")
                }
            }
        }.start()
    }
}