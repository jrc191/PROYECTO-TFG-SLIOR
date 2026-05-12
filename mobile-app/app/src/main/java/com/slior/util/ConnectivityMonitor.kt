package com.slior.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

interface ConnectivityMonitor {
    val isConnected: Flow<Boolean>
}

@Singleton
class ConnectivityMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityMonitor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Usamos un MutableStateFlow para tener un valor inicial inmediato y suscribirnos a cambios
    private val _isConnected = MutableStateFlow(checkCurrentConnectivity())
    override val isConnected: Flow<Boolean> = _isConnected.asStateFlow()

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnected.value = true
            }

            override fun onLost(network: Network) {
                // Al perder una red, verificamos si queda alguna otra activa
                _isConnected.value = checkCurrentConnectivity()
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                _isConnected.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        })
    }

    private fun checkCurrentConnectivity(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
