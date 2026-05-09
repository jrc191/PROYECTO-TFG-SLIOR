package com.slior.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalEventBus @Inject constructor() {
    private val _unauthorizedEvent = MutableSharedFlow<Unit>(replay = 0)
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    private val _connectivityEvent = MutableSharedFlow<Boolean>(replay = 1)
    val connectivityEvent = _connectivityEvent.asSharedFlow()

    suspend fun emitUnauthorized() {
        _unauthorizedEvent.emit(Unit)
    }

    suspend fun emitConnectivityChanged(isConnected: Boolean) {
        _connectivityEvent.emit(isConnected)
    }
}
