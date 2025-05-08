package com.example.weather.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

/**
 * Utility class for handling network connectivity checks and monitoring
 */
class NetworkUtils(
    private val context: Context
) {
    // Callback interface for network state changes
    interface NetworkStateListener {
        fun onNetworkAvailable()
        fun onNetworkLost()
    }

    private var networkStateListener: NetworkStateListener? = null
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if network is currently available
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    /**
     * Start monitoring for network state changes
     */
    fun startNetworkMonitoring(listener: NetworkStateListener) {
        this.networkStateListener = listener

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NetworkUtils", "Network available")
                networkStateListener?.onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                Log.d("NetworkUtils", "Network lost")
                networkStateListener?.onNetworkLost()
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Stop monitoring network state changes
     */
    fun stopNetworkMonitoring() {
        // Unregister callbacks if needed
        this.networkStateListener = null
    }

    /**
     * Get connection type (WiFi, Cellular, None)
     */
    fun getConnectionType(): String {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return when {
            capabilities == null -> "None"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            else -> "Unknown"
        }
    }
}